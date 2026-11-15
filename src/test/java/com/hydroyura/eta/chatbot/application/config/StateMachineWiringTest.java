package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.config.properties.ChatbotSearchProperties;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.application.statemachine.handler.Handler;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.generator.api.document.ExercisePdfBundle;
import com.hydroyura.eta.generator.api.document.ExercisePdfDocument;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.GRAMMAR_RULE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StateMachineWiringTest {

    private static final String SENTINEL = "__DEFAULT__";

    private StateMachine stateMachine;

    private StudentQuery studentQuery;

    private FindWords findWords;

    @BeforeEach
    void setUp() {
        var findTeacher = mock(FindTeacher.class);
        studentQuery = mock(StudentQuery.class);
        var findActiveLesson = mock(FindActiveLesson.class);
        var endLesson = mock(EndLesson.class);
        findWords = mock(FindWords.class);
        var startLesson = mock(StartLesson.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);
        var registerTeacher = mock(RegisterTeacher.class);
        var addWordToDictionary = mock(AddWordToDictionary.class);
        var addWordToLesson = mock(AddWordToLesson.class);
        var changeStudentLevel = mock(ChangeStudentLevel.class);
        var generateExercise = mock(GenerateExercise.class);
        var checkExercise = mock(CheckExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);
        var checkWord = mock(CheckWord.class);
        var wordTranslator = mock(WordTranslator.class);
        var resetWordLearning = mock(ResetWordLearning.class);

        when(studentQuery.getDictionaryId(any())).thenReturn(Optional.of(
                com.hydroyura.eta.dictionary.api.dictionary.DictionaryId.generate()));
        when(studentQuery.getCefrLevel(any())).thenReturn(Optional.of(CefrLevel.B1));
        when(studentQuery.findStudentDetails(any())).thenReturn(Optional.of(
                new StudentDetails("Anna", new DictionaryStats(0, 0, 0, 0), false, CefrLevel.B1)));
        when(findWords.findByDictionaryId(any())).thenReturn(java.util.Set.of());
        when(findWords.findByDictionaryIdAndWordIds(any(), any())).thenReturn(java.util.Set.of());
        when(generateExercise.execute(any())).thenReturn(new ExerciseDto(ExerciseId.generate(),
                ExerciseType.FILL_IN_THE_BLANK, "Animals", "content", List.of("answer"), List.of(),
                ExerciseStatus.GENERATED));
        when(generateExercisePdf.execute(any())).thenReturn(new ExercisePdfBundle(
                new ExercisePdfDocument("exercise_animals.pdf", new byte[]{1}),
                new ExercisePdfDocument("answers_animals.pdf", new byte[]{2})));

        var handlers = Map.<ChatState, Handler>of(
                ChatState.STUDENT_DICTIONARY, sentinelHandler(),
                ChatState.AWAITING_EXERCISE_WORD_SELECTION, sentinelHandler(),
                ChatState.STUDENT_OPTIONS, sentinelHandler());

        stateMachine = new StateMachineConfig().stateMachine(handlers, findTeacher, studentQuery, findActiveLesson,
                endLesson, findWords, startLesson, createStudentWithDictionary, registerTeacher, addWordToDictionary,
                addWordToLesson, changeStudentLevel, generateExercise, checkExercise, generateExercisePdf, checkWord,
                wordTranslator, resetWordLearning, new ChatbotSearchProperties(), new ChatbotPaginationProperties());
    }

    @Test
    void dictionaryCallbacksShouldBeWired() {
        var uuid = UUID.randomUUID().toString();
        assertNotDefault(ChatState.STUDENT_DICTIONARY, new Action.Callback("wdpg", "prev", 1));
        assertNotDefault(ChatState.STUDENT_DICTIONARY, new Action.Callback("wdpg", "next", 1));
        assertNotDefault(ChatState.STUDENT_DICTIONARY, new Action.Callback("wdreset", uuid, 1));
        assertNotDefault(ChatState.STUDENT_DICTIONARY, new Action.Callback("wdback", "", 1));
    }

    @Test
    void wordSelectionCallbacksShouldBeWired() {
        var uuid = UUID.randomUUID().toString();
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("exword", uuid, 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("exfilter", "all", 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("expg", "prev", 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("expg", "next", 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("exdone", "", 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("excont", "", 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("exback", "", 1));
        assertNotDefault(ChatState.AWAITING_EXERCISE_WORD_SELECTION, new Action.Callback("exselback", "", 1));
    }

    @Test
    void dictionaryActionShouldBeWiredFromStudentOptions() {
        assertNotDefault(ChatState.STUDENT_OPTIONS, new Action.Callback("action", "dictionary", 1));
    }

    private void assertNotDefault(ChatState state, Action.Callback callback) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), UUID.randomUUID().toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");

        if (state == ChatState.AWAITING_EXERCISE_WORD_SELECTION) {
            chat.getContext().put(EXERCISE_TYPE.getValue(), ExerciseType.FILL_IN_THE_BLANK);
            chat.getContext().put(GRAMMAR_RULE.getValue(), "Past Simple");
            chat.getContext().put(EXERCISE_TOPIC.getValue(), "Animals");
            chat.getContext().put(WORD_SELECTION.getValue(), java.util.Set.of());
            chat.getContext().put(WORD_FILTER.getValue(), "ALL");
            chat.getContext().put(SELECTION_PAGE.getValue(), 0);
        }

        var result = stateMachine.applyAction(chat, callback);

        assertThat(textOf(result))
                .as("callback %s:%s from %s should be wired", callback.prefix(), callback.payload(), state)
                .isNotEqualTo(SENTINEL);
    }

    private Handler sentinelHandler() {
        return new Handler() {

            @Override
            public ActionResult handle(Chat chat) {
                return new ActionResult.TextResponse(SENTINEL);
            }

            @Override
            public ChatState getChatState() {
                return ChatState.STUDENT_DICTIONARY;
            }
        };
    }

    private String textOf(ActionResult result) {
        return switch (result) {
            case ActionResult.TextResponse(var text) -> text;
            case ActionResult.EditMessageText(var messageId, var text, var keyboard) -> text;
            case ActionResult.TextWithInlineKeyboard(var text, var keyboard) -> text;
            case ActionResult.TextWithReplyKeyboard(var text, var keyboard, var cleanupMessageId) -> text;
            default -> "";
        };
    }
}
