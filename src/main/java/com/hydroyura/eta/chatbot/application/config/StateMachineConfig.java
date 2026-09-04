package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.handler.Handler;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.DefaultCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.NewCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.NewStudentTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos.PosCbAwaitingPosTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingregistrationname.InputAwaitingRegistrationNameTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentname.InputAwaitingStudentNameTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslation.InputAwaitingTranslationTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingword.InputAwaitingWordTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexerciseanswer.InputAwaitingExerciseAnswerTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.InputAwaitingExerciseTopicTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetype.ExerciseCbAwaitingExerciseTypeTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.initial.DefaultCmdInitialTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.initial.RegisterCmdInitialTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.AddWordInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.FinishLessonInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.HelpCmdInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails.BackCbStudentDetailsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentoptions.ActionCbStudentOptionsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.BackCbStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.ListCmdStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.StudentCbStudentsListTransition;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Commands;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hydroyura.eta.chatbot.domain.chat.ChatState.ACTIVE;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_ANSWER;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_REGISTRATION_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_TRANSLATION;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_WORD;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.IN_LESSON;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.INITIAL;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENT_DETAILS;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENT_OPTIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENTS_LIST;

@Configuration
public class StateMachineConfig {

    @Bean
    public Map<ChatState, Handler> chatStateHandlerMap(List<Handler> handlers) {
        return handlers.stream().collect(Collectors.toMap(Handler::getChatState, Function.identity()));
    }

    @Bean
    public StateMachine stateMachine(Map<ChatState, Handler> chatStateHandlerMap, FindTeacher findTeacher, StudentQuery studentQuery,
                                     FindActiveLesson findActiveLesson, EndLesson endLesson, FindWords findWords,
                                     StartLesson startLesson, CreateStudentWithDictionary createStudentWithDictionary,
                                     RegisterTeacher registerTeacher, AddWordToDictionary addWordToDictionary,
                                     AddWordToLesson addWordToLesson, GenerateExercise generateExercise, CheckExercise checkExercise) {
        StateMachine stateMachine = new StateMachine(chatStateHandlerMap);

        // переходы, регистрируемые под несколькими ключами
        var defaultCmdInitialTransition = new DefaultCmdInitialTransition();
        var defaultCmdActiveTransition = new DefaultCmdActiveTransition();
        var studentListTransition = new StudentListTransition(findTeacher, studentQuery);
        var newStudentTransition = new NewStudentTransition();
        var listCmdStudentsListTransition = new ListCmdStudentsListTransition(findTeacher, studentQuery);
        var addWordInLessonTransition = new AddWordInLessonTransition();
        var finishLessonInLessonTransition = new FinishLessonInLessonTransition(findActiveLesson, endLesson, studentQuery, findWords);

        // INITIAL
        stateMachine.onCommand(INITIAL, Commands.REGISTER, new RegisterCmdInitialTransition());
        stateMachine.onCommand(INITIAL, Commands.START, defaultCmdInitialTransition);
        stateMachine.onCommand(INITIAL, Commands.HELP, defaultCmdInitialTransition);

        // ACTIVE
        stateMachine.onCommand(ACTIVE, Commands.NEW, new NewCmdActiveTransition());
        stateMachine.onCommand(ACTIVE, Commands.LIST, studentListTransition);
        stateMachine.onCommand(ACTIVE, Commands.HELP, defaultCmdActiveTransition);
        stateMachine.onCommand(ACTIVE, Commands.START, defaultCmdActiveTransition);
        stateMachine.onButton(ACTIVE, Buttons.NEW_STUDENT, newStudentTransition);
        stateMachine.onButton(ACTIVE, Buttons.LIST_STUDENT, studentListTransition);

        // AWAITING_REGISTRATION_NAME
        stateMachine.onInput(AWAITING_REGISTRATION_NAME, new InputAwaitingRegistrationNameTransition(registerTeacher));

        // AWAITING_STUDENT_NAME
        stateMachine.onInput(AWAITING_STUDENT_NAME, new InputAwaitingStudentNameTransition(findTeacher, createStudentWithDictionary));

        // STUDENTS_LIST
        stateMachine.onCallback(STUDENTS_LIST, Callbacks.STUDENT, new StudentCbStudentsListTransition(findTeacher, studentQuery));
        stateMachine.onCallback(STUDENTS_LIST, Callbacks.BACK, new BackCbStudentsListTransition());
        stateMachine.onCommand(STUDENTS_LIST, Commands.NEW, newStudentTransition);
        stateMachine.onCommand(STUDENTS_LIST, Commands.LIST, listCmdStudentsListTransition);
        stateMachine.onCommand(STUDENTS_LIST, Commands.HELP, listCmdStudentsListTransition);

        // STUDENT_OPTIONS
        stateMachine.onCallback(STUDENT_OPTIONS, Callbacks.ACTION, new ActionCbStudentOptionsTransition(startLesson, findTeacher, studentQuery));

        // STUDENT_DETAILS
        stateMachine.onCallback(STUDENT_DETAILS, Callbacks.DETAILS, new BackCbStudentDetailsTransition(findTeacher, studentQuery));

        // IN_LESSON
        stateMachine.onButton(IN_LESSON, Buttons.ADD_WORD, addWordInLessonTransition);
        stateMachine.onCommand(IN_LESSON, Commands.ADD_WORD, addWordInLessonTransition);
        stateMachine.onButton(IN_LESSON, Buttons.FINISH_LESSON, finishLessonInLessonTransition);
        stateMachine.onCommand(IN_LESSON, Commands.FINISH_LESSON, finishLessonInLessonTransition);
        stateMachine.onCommand(IN_LESSON, Commands.HELP, new HelpCmdInLessonTransition());

        // word flow: AWAITING_WORD -> AWAITING_POS -> AWAITING_TRANSLATION
        stateMachine.onInput(AWAITING_WORD, new InputAwaitingWordTransition());
        stateMachine.onCallback(AWAITING_POS, Callbacks.POS, new PosCbAwaitingPosTransition());
        stateMachine.onInput(AWAITING_TRANSLATION, new InputAwaitingTranslationTransition(studentQuery, addWordToDictionary, findActiveLesson, addWordToLesson));

        // exercise flow: AWAITING_EXERCISE_TYPE -> AWAITING_EXERCISE_TOPIC -> AWAITING_EXERCISE_ANSWER
        stateMachine.onCallback(AWAITING_EXERCISE_TYPE, Callbacks.EXERCISE, new ExerciseCbAwaitingExerciseTypeTransition());
        stateMachine.onInput(AWAITING_EXERCISE_TOPIC, new InputAwaitingExerciseTopicTransition(studentQuery, generateExercise));
        stateMachine.onInput(AWAITING_EXERCISE_ANSWER, new InputAwaitingExerciseAnswerTransition(checkExercise));

        if (!stateMachine.isReady()) {
            throw new RuntimeException();
        }

        return stateMachine;
    }

}
