package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.generator.api.document.ExercisePdfBundle;
import com.hydroyura.eta.generator.api.document.ExercisePdfDocument;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.generator.api.document.PdfRenderException;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.GRAMMAR_RULE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.EXERCISE_PDF_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputAwaitingExerciseTopicTransitionTest {

    @Test
    void shouldReturnSendDocumentsOnSuccess() {
        var studentQuery = mock(StudentQuery.class);
        var generateExercise = mock(GenerateExercise.class);
        var findWords = mock(FindWords.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);

        var studentId = StudentId.generate();
        var dictionaryId = DictionaryId.generate();

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(generateExercise.execute(any())).thenReturn(exercise());
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(word()));
        when(generateExercisePdf.execute(any())).thenReturn(new ExercisePdfBundle(
                new ExercisePdfDocument("exercise_animals.pdf", new byte[]{1}),
                new ExercisePdfDocument("answers_animals.pdf", new byte[]{2})));

        var transition = new InputAwaitingExerciseTopicTransition(studentQuery, generateExercise, findWords,
                generateExercisePdf);

        var chat = chat(studentId);
        chat.getContext().put(GRAMMAR_RULE.getValue(), "Past Simple");
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);

        var result = transition.transit(chat, new Action.Input("Animals"));

        assertThat(result).isInstanceOf(ActionResult.SendDocuments.class);
        var sendDocuments = (ActionResult.SendDocuments) result;
        assertThat(sendDocuments.documents()).hasSize(2);
        assertThat(sendDocuments.text()).isEqualTo("Ученик: Alice");
        assertThat(sendDocuments.keyboard()).isNotEmpty();
    }

    @Test
    void shouldReturnErrorMessageOnRenderFailure() {
        var studentQuery = mock(StudentQuery.class);
        var generateExercise = mock(GenerateExercise.class);
        var findWords = mock(FindWords.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);

        var studentId = StudentId.generate();
        var dictionaryId = DictionaryId.generate();

        when(studentQuery.getDictionaryId(studentId)).thenReturn(Optional.of(dictionaryId));
        when(generateExercise.execute(any())).thenReturn(exercise());
        when(findWords.findByDictionaryId(dictionaryId)).thenReturn(Set.of(word()));
        when(generateExercisePdf.execute(any())).thenThrow(new PdfRenderException("boom"));

        var transition = new InputAwaitingExerciseTopicTransition(studentQuery, generateExercise, findWords,
                generateExercisePdf);

        var chat = chat(studentId);
        chat.getContext().put(GRAMMAR_RULE.getValue(), "Past Simple");
        chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);

        var result = transition.transit(chat, new Action.Input("Animals"));

        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
        assertThat(((ActionResult.TextWithInlineKeyboard) result).text()).isEqualTo(EXERCISE_PDF_ERROR);
    }

    private Chat chat(StudentId studentId) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId.value().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Alice");
        return chat;
    }

    private ExerciseDto exercise() {
        return new ExerciseDto(
                ExerciseId.generate(),
                ExerciseType.FILL_IN_THE_BLANK,
                "Animals",
                "1. I travel ___",
                List.of("travelled"),
                List.of(new ExerciseItem("I travel ___", List.of(), "travelled")),
                ExerciseStatus.GENERATED
        );
    }

    private WordProjection word() {
        return new WordProjection(WordId.generate(), "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                WordStatus.IN_PROGRESS);
    }
}
