package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotExerciseGenerationProperties;
import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.config.properties.ChatbotSearchProperties;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.application.statemachine.handler.Handler;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.notification.BotNotifier;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopics;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopic;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.DeleteStudent;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.support.api.support.OpenSupportThread;
import com.hydroyura.eta.teacher.api.teacher.CompleteOnboarding;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTIVE_LESSON_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TEACHER_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_SEARCH_QUERY;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.view.Messages.CANCELLED;
import static com.hydroyura.eta.chatbot.view.Messages.NOTHING_TO_CANCEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StateMachineCancelTest {

    private StateMachine stateMachine;

    private StudentQuery studentQuery;

    private static final UUID STUDENT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        var findTeacher = mock(FindTeacher.class);
        studentQuery = mock(StudentQuery.class);
        var findActiveLesson = mock(FindActiveLesson.class);
        var endLesson = mock(EndLesson.class);
        var findWords = mock(FindWords.class);
        var startLesson = mock(StartLesson.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);
        var registerTeacher = mock(RegisterTeacher.class);
        var completeOnboarding = mock(CompleteOnboarding.class);
        var addWordToDictionary = mock(AddWordToDictionary.class);
        var addWordToLesson = mock(AddWordToLesson.class);
        var changeStudentLevel = mock(ChangeStudentLevel.class);
        var deleteStudent = mock(DeleteStudent.class);
        var generateExercise = mock(GenerateExercise.class);
        var checkExercise = mock(CheckExercise.class);
        var generateExercisePdf = mock(GenerateExercisePdf.class);
        var checkWord = mock(CheckWord.class);
        var wordTranslator = mock(WordTranslator.class);
        var resetWordLearning = mock(ResetWordLearning.class);
        var exerciseTopics = mock(ExerciseTopics.class);
        var validateCustomTopic = mock(ValidateCustomTopic.class);
        var teacherDirectory = mock(TeacherDirectory.class);
        var openSupportThread = mock(OpenSupportThread.class);
        var appendSupportMessage = mock(AppendSupportMessage.class);
        var findSupportThreads = mock(FindSupportThreads.class);
        var findSupportMessages = mock(FindSupportMessages.class);
        var botNotifier = mock(BotNotifier.class);

        when(studentQuery.findStudentDetails(any())).thenReturn(Optional.of(
                new StudentDetails("Anna", new DictionaryStats(0, 0, 0, 0), false, CefrLevel.B1)));

        stateMachine = new StateMachineConfig().stateMachine(Map.<ChatState, Handler>of(), findTeacher, studentQuery,
                findActiveLesson, endLesson, findWords, startLesson, createStudentWithDictionary, registerTeacher,
                completeOnboarding, addWordToDictionary, addWordToLesson, changeStudentLevel, deleteStudent,
                generateExercise, checkExercise, generateExercisePdf, checkWord, wordTranslator, resetWordLearning,
                exerciseTopics, validateCustomTopic, new ChatbotSearchProperties(),
                new ChatbotPaginationProperties(), new ChatbotExerciseGenerationProperties(), teacherDirectory,
                openSupportThread, appendSupportMessage,
                findSupportThreads, findSupportMessages, botNotifier);
    }

    @Test
    void cancelShouldBeRegisteredInEveryDialogState() {
        var dialogStates = List.of(
                new Case(ChatState.AWAITING_REGISTRATION_NAME, ChatState.INITIAL),
                new Case(ChatState.AWAITING_STUDENT_NAME, ChatState.ACTIVE),
                new Case(ChatState.AWAITING_STUDENT_LEVEL, ChatState.ACTIVE),
                new Case(ChatState.AWAITING_STUDENT_LEVEL_CHANGE, ChatState.STUDENT_DETAILS),
                new Case(ChatState.AWAITING_STUDENT_DELETE, ChatState.STUDENT_DETAILS),
                new Case(ChatState.AWAITING_POS, ChatState.IN_LESSON),
                new Case(ChatState.AWAITING_TRANSLATION_CHOICE, ChatState.IN_LESSON),
                new Case(ChatState.AWAITING_MANUAL_TRANSLATION, ChatState.IN_LESSON),
                new Case(ChatState.STUDENT_SEARCH, ChatState.ACTIVE),
                new Case(ChatState.AWAITING_EXERCISE_TYPE, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.AWAITING_EXERCISE_TOPIC, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.AWAITING_CUSTOM_TOPIC, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.AWAITING_EXERCISE_WORD_SELECTION, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.AWAITING_EXERCISE_ANSWER, ChatState.STUDENT_OPTIONS),
                new Case(ChatState.STUDENT_DICTIONARY, ChatState.STUDENT_DETAILS),
                new Case(ChatState.ACCOUNT, ChatState.ACTIVE),
                new Case(ChatState.SUPPORT, ChatState.ACTIVE),
                new Case(ChatState.ADMIN_TEACHERS, ChatState.ADMIN_MENU),
                new Case(ChatState.ADMIN_TEACHER_STUDENTS, ChatState.ADMIN_MENU),
                new Case(ChatState.ADMIN_THREADS, ChatState.ADMIN_MENU),
                new Case(ChatState.ADMIN_THREAD_VIEW, ChatState.ADMIN_MENU),
                new Case(ChatState.ADMIN_MENU, ChatState.ADMIN_MENU));

        for (var dialog : dialogStates) {
            var chat = chatWithSharedKeys(dialog.from());
            var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

            assertThat(chat.getState())
                    .as("state for %s", dialog.from())
                    .isEqualTo(dialog.to());
            assertThat(textOf(result)).contains(CANCELLED);
            assertSharedKeysPreserved(chat);
        }
    }

    @Test
    void cancelFromDictionaryShouldClearDictionaryPage() {
        var chat = chatWithSharedKeys(ChatState.STUDENT_DICTIONARY);
        chat.getContext().put(DICTIONARY_PAGE.getValue(), 2);

        var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_DETAILS);
        assertThat(chat.getContext()).doesNotContainKey(DICTIONARY_PAGE.getValue());
        assertSharedKeysPreserved(chat);
        assertThat(textOf(result)).contains(CANCELLED);
    }

    @Test
    void cancelFromWordSelectionShouldClearFlowKeys() {
        var chat = chatWithSharedKeys(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        chat.getContext().put(EXERCISE_TYPE.getValue(),
                com.hydroyura.eta.exercise.api.exercise.ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "Travel");
        chat.getContext().put(WORD_SELECTION.getValue(), Set.of("8a86f6a5-85d4-4b9f-9c6b-5f0f4d0a6f6b"));
        chat.getContext().put(WORD_FILTER.getValue(), "IN_PROGRESS");
        chat.getContext().put(SELECTION_PAGE.getValue(), 1);

        var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue(), WORD_SELECTION.getValue(), WORD_FILTER.getValue(),
                SELECTION_PAGE.getValue());
        assertSharedKeysPreserved(chat);
        assertThat(textOf(result)).contains(CANCELLED);
    }

    @Test
    void cancelFromThematicTopicShouldClearFlowKeys() {
        var chat = chatWithSharedKeys(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);
        chat.getContext().put(EXERCISE_TYPE.getValue(),
                com.hydroyura.eta.exercise.api.exercise.ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(EXERCISE_TOPIC.getValue(), new CatalogTopic("present-simple"));
        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), "Travel");

        var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), EXERCISE_TOPIC.getValue(),
                EXERCISE_THEMATIC_TOPIC.getValue());
        assertSharedKeysPreserved(chat);
        assertThat(textOf(result)).contains(CANCELLED);
    }

    @Test
    void cancelFromTopicSelectionShouldClearTopicKeys() {
        var chat = chatWithSharedKeys(ChatState.AWAITING_EXERCISE_TOPIC);
        chat.getContext().put(EXERCISE_TYPE.getValue(),
                com.hydroyura.eta.exercise.api.exercise.ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(TOPIC_SEARCH_QUERY.getValue(), "past");
        chat.getContext().put(TOPIC_PAGE.getValue(), 1);

        var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), TOPIC_SEARCH_QUERY.getValue(),
                TOPIC_PAGE.getValue());
        assertSharedKeysPreserved(chat);
        assertThat(textOf(result)).contains(CANCELLED);
    }

    @Test
    void cancelFromTopicConfirmShouldClearValidationKeys() {
        var chat = chatWithSharedKeys(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        chat.getContext().put(EXERCISE_TYPE.getValue(),
                com.hydroyura.eta.exercise.api.exercise.ExerciseType.FILL_IN_THE_BLANK);
        chat.getContext().put(TOPIC_CANONICAL_NAME.getValue(), "Past Simple");
        chat.getContext().put(TOPIC_MATCHED_SLUG.getValue(), "past-simple");

        var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

        assertThat(chat.getState()).isEqualTo(ChatState.STUDENT_OPTIONS);
        assertThat(chat.getContext()).doesNotContainKeys(EXERCISE_TYPE.getValue(), TOPIC_CANONICAL_NAME.getValue(),
                TOPIC_MATCHED_SLUG.getValue());
        assertSharedKeysPreserved(chat);
        assertThat(textOf(result)).contains(CANCELLED);
    }

    @Test
    void cancelShouldReturnNothingToCancelInIdleStates() {
        var idleStates = List.of(
                ChatState.INITIAL,
                ChatState.ACTIVE,
                ChatState.STUDENTS_LIST,
                ChatState.STUDENT_OPTIONS,
                ChatState.STUDENT_DETAILS,
                ChatState.IN_LESSON);

        for (var state : idleStates) {
            var chat = chatWithSharedKeys(state);
            var result = stateMachine.applyAction(chat, new Action.Command("/cancel"));

            assertThat(chat.getState()).as("state for %s", state).isEqualTo(state);
            assertThat(textOf(result)).isEqualTo(NOTHING_TO_CANCEL);
        }
    }

    @Test
    void cancelShouldClearSupportAndAdminSelectionKeys() {
        var supportChat = chatWithSharedKeys(ChatState.SUPPORT);
        supportChat.getContext().put(SUPPORT_THREAD_ID.getValue(), "thread-1");

        stateMachine.applyAction(supportChat, new Action.Command("/cancel"));

        assertThat(supportChat.getState()).isEqualTo(ChatState.ACTIVE);
        assertThat(supportChat.getContext()).doesNotContainKey(SUPPORT_THREAD_ID.getValue());

        for (var state : List.of(ChatState.ADMIN_TEACHERS, ChatState.ADMIN_TEACHER_STUDENTS,
                ChatState.ADMIN_THREADS, ChatState.ADMIN_THREAD_VIEW, ChatState.ADMIN_MENU)) {
            var chat = chatWithSharedKeys(state);
            chat.getContext().put(ADMIN_SELECTED_TEACHER_ID.getValue(), "teacher-1");
            chat.getContext().put(ADMIN_SELECTED_TEACHER_NAME.getValue(), "Teacher");
            chat.getContext().put(SUPPORT_THREAD_ID.getValue(), "thread-1");

            stateMachine.applyAction(chat, new Action.Command("/cancel"));

            assertThat(chat.getState()).as("state for %s", state).isEqualTo(ChatState.ADMIN_MENU);
            assertThat(chat.getContext()).doesNotContainKeys(ADMIN_SELECTED_TEACHER_ID.getValue(),
                    ADMIN_SELECTED_TEACHER_NAME.getValue(), SUPPORT_THREAD_ID.getValue());
        }
    }

    private Chat chatWithSharedKeys(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), STUDENT_UUID.toString());
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), "Anna");
        chat.getContext().put(ACTIVE_LESSON_ID.getValue(), "lesson");
        chat.getContext().put(TEACHER_NAME.getValue(), "Teacher");
        return chat;
    }

    private void assertSharedKeysPreserved(Chat chat) {
        assertThat(chat.getContext()).containsKeys(SELECTED_STUDENT_ID.getValue(), SELECTED_STUDENT_NAME.getValue(),
                ACTIVE_LESSON_ID.getValue(), TEACHER_NAME.getValue());
    }

    private String textOf(ActionResult result) {
        return switch (result) {
            case ActionResult.TextResponse(var text) -> text;
            case ActionResult.TextWithReplyKeyboard(var text, var keyboard, var cleanupMessageId) -> text;
            case ActionResult.TextWithInlineKeyboard(var text, var keyboard) -> text;
            default -> "";
        };
    }

    private record Case(ChatState from, ChatState to) {
    }
}
