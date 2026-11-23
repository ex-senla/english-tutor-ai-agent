package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotExerciseGenerationProperties;
import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseGenerationException;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.generator.api.document.ExercisePdfBundle;
import com.hydroyura.eta.generator.api.document.ExercisePdfDocument;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.generator.api.document.PdfRenderException;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_PDF_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AwaitingExerciseWordSelectionTransitionTest {

    private StudentQuery studentQuery;

    private FindWords findWords;

    private ExerciseWordSelectionSupport support;

    private StudentId studentId;

    private DictionaryId dictionaryId;

    @BeforeEach
    void setUp() {
        studentQuery = mock(StudentQuery.class);
        findWords = mock(FindWords.class);
        support = new ExerciseWordSelectionSupport(studentQuery, findWords, new ChatbotPaginationProperties());

        studentId = StudentId.generate();
        dictionaryId = DictionaryId.generate();
        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
    }

    @Test
    void shouldToggleWordSelection() {
        var appleId = WordInfoId.generate();
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));

        var transition = new WordToggleCbAwaitingExerciseWordSelectionTransition(support);
        var chat = chatWithSelection(Set.of(appleId.value().toString()));

        transition.transit(chat, new Action.Callback("exword", appleId.value().toString(), 1));
        assertThat(chat.getContext().get(WORD_SELECTION.getValue())).isEqualTo(Set.of());

        transition.transit(chat, new Action.Callback("exword", appleId.value().toString(), 1));
        assertThat(chat.getContext().get(WORD_SELECTION.getValue())).isEqualTo(Set.of(appleId.value().toString()));
    }

    @Test
    void shouldChangeFilterAndResetPage() {
        var appleId = WordInfoId.generate();
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));

        var transition = new FilterCbAwaitingExerciseWordSelectionTransition(support);
        var chat = chatWithSelection(Set.of(appleId.value().toString()));
        chat.getContext().put(SELECTION_PAGE.getValue(), 1);

        transition.transit(chat, new Action.Callback("exfilter", "new", 1));

        assertThat(chat.getContext().get(WORD_FILTER.getValue())).isEqualTo("NEW");
        assertThat(chat.getContext().get(SELECTION_PAGE.getValue())).isEqualTo(0);
        assertThat(chat.getContext().get(WORD_SELECTION.getValue())).isEqualTo(Set.of(appleId.value().toString()));
    }

    @Test
    void shouldChangePage() {
        var words = IntStream.range(0, 10)
                .mapToObj(i -> new WordProjection(WordInfoId.generate(), "word" + i, Set.of("перевод"),
                        PartOfSpeech.NOUN, WordStatus.IN_PROGRESS))
                .collect(Collectors.toSet());
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(words);

        var transition = new PageCbAwaitingExerciseWordSelectionTransition(support);
        var chat = chatWithSelection(Set.of());
        chat.getContext().put(WORD_FILTER.getValue(), "ALL");

        transition.transit(chat, new Action.Callback("expg", "next", 1));
        assertThat(chat.getContext().get(SELECTION_PAGE.getValue())).isEqualTo(1);

        transition.transit(chat, new Action.Callback("expg", "prev", 1));
        assertThat(chat.getContext().get(SELECTION_PAGE.getValue())).isEqualTo(0);
    }

    @Test
    void shouldWarnWhenDoneWithNoSelection() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(true, support, generateExercise,
                generateExercisePdf, new ChatbotExerciseGenerationProperties());
        var chat = chatWithSelection(Set.of());

        var result = transition.transit(chat, new Action.Callback("exdone", "", 1));

        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
        assertThat(((ActionResult.EditMessageText) result).text()).contains("не выбрали ни одного слова");
        verifyNoInteractions(generateExercise, generateExercisePdf);
    }

    @Test
    void shouldGenerateWithSelectedWords() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);

        var appleId = WordInfoId.generate();
        when(studentQuery.getCefrLevel(studentId)).thenReturn(Optional.of(CefrLevel.B1));
        when(findWords.findByDictionaryIdAndWordIds(dictionaryId, Set.of(appleId))).thenReturn(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));
        when(generateExercise.execute(any())).thenReturn(exercise());
        when(generateExercisePdf.execute(any())).thenReturn(new ExercisePdfBundle(
                new ExercisePdfDocument("exercise_animals.pdf", new byte[]{1}),
                new ExercisePdfDocument("answers_animals.pdf", new byte[]{2})));

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(true, support, generateExercise,
                generateExercisePdf, new ChatbotExerciseGenerationProperties());
        var chat = chatWithSelection(Set.of(appleId.value().toString()));
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "Travel");

        var result = transition.transit(chat, new Action.Callback("exdone", "", 1));

        assertThat(result).isInstanceOf(ActionResult.SendDocuments.class);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue(), WORD_SELECTION.getValue(), WORD_FILTER.getValue(),
                SELECTION_PAGE.getValue());

        var captor = ArgumentCaptor.forClass(GenerateExerciseCommand.class);
        verify(generateExercise).execute(captor.capture());
        assertThat(captor.getValue().wordIds()).containsExactly(appleId);
        assertThat(captor.getValue().thematicTopic()).isEqualTo("Travel");
    }

    @Test
    void shouldGenerateWithoutWordsOnContinue() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);

        when(studentQuery.getCefrLevel(studentId)).thenReturn(Optional.of(CefrLevel.B1));
        when(generateExercise.execute(any())).thenReturn(exercise());
        when(generateExercisePdf.execute(any())).thenReturn(new ExercisePdfBundle(
                new ExercisePdfDocument("exercise_animals.pdf", new byte[]{1}),
                new ExercisePdfDocument("answers_animals.pdf", new byte[]{2})));

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(false, support, generateExercise,
                generateExercisePdf, new ChatbotExerciseGenerationProperties());
        var chat = chatWithSelection(Set.of());
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));

        var result = transition.transit(chat, new Action.Callback("excont", "", 1));

        assertThat(result).isInstanceOf(ActionResult.SendDocuments.class);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);

        var captor = ArgumentCaptor.forClass(GenerateExerciseCommand.class);
        verify(generateExercise).execute(captor.capture());
        assertThat(captor.getValue().wordIds()).isEmpty();
    }

    @Test
    void shouldReturnToStudentOptionsOnSelectionBack() {
        var transition = new SelectionBackCbAwaitingExerciseWordSelectionTransition();
        var chat = chatWithSelection(Set.of());
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "Travel");

        var result = transition.transit(chat, new Action.Callback("exselback", "", 1));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue(), WORD_SELECTION.getValue(), WORD_FILTER.getValue(),
                SELECTION_PAGE.getValue());
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldReturnPdfErrorAndResetFlowOnRenderFailure() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);

        var appleId = WordInfoId.generate();
        when(studentQuery.getCefrLevel(studentId)).thenReturn(Optional.of(CefrLevel.B1));
        when(findWords.findByDictionaryIdAndWordIds(dictionaryId, Set.of(appleId))).thenReturn(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));
        when(generateExercise.execute(any())).thenReturn(exercise());
        when(generateExercisePdf.execute(any())).thenThrow(new PdfRenderException("boom"));

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(true, support, generateExercise,
                generateExercisePdf, new ChatbotExerciseGenerationProperties());
        var chat = chatWithSelection(Set.of(appleId.value().toString()));
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "Travel");

        var result = transition.transit(chat, new Action.Callback("exdone", "", 1));

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo(EXERCISE_PDF_ERROR);
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue(), WORD_SELECTION.getValue(), WORD_FILTER.getValue(),
                SELECTION_PAGE.getValue());
    }

    @Test
    void shouldReturnErrorMessageAndResetFlowOnGenerationFailureWithWords() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);
        var properties = new ChatbotExerciseGenerationProperties();
        properties.setErrorMessage("Генерация не удалась");

        var appleId = WordInfoId.generate();
        when(studentQuery.getCefrLevel(studentId)).thenReturn(Optional.of(CefrLevel.B1));
        when(generateExercise.execute(any())).thenThrow(new ExerciseGenerationException("AI down"));

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(true, support, generateExercise,
                generateExercisePdf, properties);
        var chat = chatWithSelection(Set.of(appleId.value().toString()));
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "Travel");

        var result = transition.transit(chat, new Action.Callback("exdone", "", 1));

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo("Генерация не удалась");
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue(), WORD_SELECTION.getValue(), WORD_FILTER.getValue(),
                SELECTION_PAGE.getValue());
        assertThat(chat.getContext()).containsEntry(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        assertThat(chat.getContext()).containsEntry(SELECTED_STUDENT_NAME.getValue(), "Anna");
        verifyNoInteractions(generateExercisePdf);
    }

    @Test
    void shouldReturnErrorMessageAndResetFlowOnGenerationFailureWithoutWords() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);
        var properties = new ChatbotExerciseGenerationProperties();
        properties.setErrorMessage("Генерация не удалась");

        when(studentQuery.getCefrLevel(studentId)).thenReturn(Optional.of(CefrLevel.B1));
        when(generateExercise.execute(any())).thenThrow(new ExerciseGenerationException("AI down"));

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(false, support, generateExercise,
                generateExercisePdf, properties);
        var chat = chatWithSelection(Set.of());
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));

        var result = transition.transit(chat, new Action.Callback("excont", "", 1));

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo("Генерация не удалась");
        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue(), WORD_SELECTION.getValue(), WORD_FILTER.getValue(),
                SELECTION_PAGE.getValue());
        assertThat(chat.getContext()).containsEntry(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        assertThat(chat.getContext()).containsEntry(SELECTED_STUDENT_NAME.getValue(), "Anna");
        verifyNoInteractions(generateExercisePdf);
    }

    @Test
    void shouldUseDefaultErrorMessageOnGenerationFailure() {
        var generateExercise = mock(GenerateExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);
        var properties = new ChatbotExerciseGenerationProperties();

        when(studentQuery.getCefrLevel(studentId)).thenReturn(Optional.of(CefrLevel.B1));
        when(generateExercise.execute(any())).thenThrow(new ExerciseGenerationException("AI down"));

        var transition = new GenerateCbAwaitingExerciseWordSelectionTransition(false, support, generateExercise,
                generateExercisePdf, properties);
        var chat = chatWithSelection(Set.of());
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));

        var result = transition.transit(chat, new Action.Callback("excont", "", 1));

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text())
                .isEqualTo("⚠️ Не удалось сгенерировать упражнение. Попробуйте, пожалуйста, ещё раз немного позже.");
    }

    private Chat chatWithSelection(Set<String> selection) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");
        chat.getContext().put(WORD_SELECTION.getValue(), selection);
        chat.getContext().put(WORD_FILTER.getValue(), "ALL");
        chat.getContext().put(SELECTION_PAGE.getValue(), 0);
        return chat;
    }

    private ExerciseDto exercise() {
        return new ExerciseDto(ExerciseId.generate(), ExerciseType.FILL_IN_THE_BLANK, "Animals", null, "content",
                List.of("answer"), List.of(new ExerciseItem("I travel ___", List.of(), "travelled")),
                ExerciseStatus.GENERATED);
    }
}
