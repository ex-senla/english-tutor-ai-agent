package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.config.properties.ChatbotSearchProperties;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopics;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopic;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.DeleteStudent;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StartCommandResetTest {

    private StateMachine stateMachine;

    @BeforeEach
    void setUp() {
        var findTeacher = mock(FindTeacher.class);
        var studentQuery = mock(StudentQuery.class);
        var findActiveLesson = mock(FindActiveLesson.class);
        var endLesson = mock(EndLesson.class);
        var findWords = mock(FindWords.class);
        var startLesson = mock(StartLesson.class);
        var createStudentWithDictionary = mock(CreateStudentWithDictionary.class);
        var registerTeacher = mock(RegisterTeacher.class);
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

        stateMachine = new StateMachineConfig().stateMachine(Map.of(), findTeacher, studentQuery, findActiveLesson,
                endLesson, findWords, startLesson, createStudentWithDictionary, registerTeacher, addWordToDictionary,
                addWordToLesson, changeStudentLevel, deleteStudent, generateExercise, checkExercise,
                generateExercisePdf, checkWord, wordTranslator, resetWordLearning, exerciseTopics,
                validateCustomTopic, new ChatbotSearchProperties(), new ChatbotPaginationProperties());
    }

    @Test
    void startShouldResetToActiveFromStuckStates() {
        var stuckStates = List.of(
                ChatState.STUDENTS_LIST,
                ChatState.STUDENT_OPTIONS,
                ChatState.IN_LESSON,
                ChatState.AWAITING_STUDENT_NAME,
                ChatState.STUDENT_DETAILS);

        for (var state : stuckStates) {
            var chat = Chat.ofDefaults(new ChatId(1L));
            chat.updateState(state);

            var result = stateMachine.applyAction(chat, new Action.Command("/start"));

            assertThat(chat.getState()).as("state after /start from %s", state).isEqualTo(ChatState.ACTIVE);
            assertThat(result).isInstanceOf(ActionResult.TextWithReplyKeyboard.class);
            assertThat(textOf(result)).contains("Главное меню");
        }
    }

    @Test
    void startFromInitialShouldStayInitial() {
        var chat = Chat.ofDefaults(new ChatId(1L));

        var result = stateMachine.applyAction(chat, new Action.Command("/start"));

        assertThat(chat.getState()).isEqualTo(ChatState.INITIAL);
        assertThat(textOf(result)).isEqualTo(Messages.WELCOME);
    }

    private String textOf(ActionResult result) {
        return switch (result) {
            case ActionResult.TextResponse(var text) -> text;
            case ActionResult.TextWithReplyKeyboard(var text, var keyboard, var cleanupMessageId) -> text;
            case ActionResult.TextWithInlineKeyboard(var text, var keyboard) -> text;
            case ActionResult.EditMessageText(var messageId, var text, var keyboard) -> text;
            default -> "";
        };
    }
}
