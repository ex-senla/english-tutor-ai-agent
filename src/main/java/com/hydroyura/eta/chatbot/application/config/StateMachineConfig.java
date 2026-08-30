package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.ChatService;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.handler.Handler;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.TransitionKey;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.DefaultCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.ListCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.MyStudentsBtnActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.NewCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.NewStudentBtnActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos.PosCbAwaitingPosTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingregistrationname.InputAwaitingRegistrationNameTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentname.InputAwaitingStudentNameTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslation.InputAwaitingTranslationTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingword.InputAwaitingWordTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awatingexerciseanswer.InputAwatingExerciseAnswerTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awatingexercisetopic.InputAwatingExerciseTopicTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awatingexercisetype.ExerciseCbAwatingExerciseTypeTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.initial.DefaultCmdInitialTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.initial.RegisterCmdInitialTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.AddWordInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.FinishLessonInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.HelpCmdInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails.BackCbStudentDetailsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentoptions.ActionCbStudentOptionsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.BackCbStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.ListCmdStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.NewCmdStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.StudentCbStudentsListTransition;
import com.hydroyura.eta.chatbot.item.Buttons;
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

import java.util.EnumMap;
import java.util.HashMap;
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

    private final Map<TransitionKey, Transition<Action>> transitions = new HashMap<>();

    private final Map<ChatState, Transition<Action>> inputTransitions = new EnumMap<>(ChatState.class);

    @Bean
    public Map<String, Transition<Action>> nameTransitionMap(List<Transition<Action>> transitions) {
        return transitions.stream().collect(Collectors.toMap(Transition::getName, Function.identity()));
    }

    @Bean
    public Map<ChatState, Handler> chatStateHandlerMap(List<Handler> handlers) {
        return handlers.stream().collect(Collectors.toMap(Handler::getChatState, Function.identity()));
    }

    // ========================================================================
    // Transitions as beans
    // ========================================================================

    // ---- INITIAL ----
    @Bean
    public Transition<Action.Command> registerCmdInitialTransition() {
        return new RegisterCmdInitialTransition();
    }

    @Bean
    public Transition<Action.Command> defaultCmdInitialTransition() {
        return new DefaultCmdInitialTransition();
    }

    // ---- ACTIVE ----
    @Bean
    public Transition<Action.Command> newCmdActiveTransition() {
        return new NewCmdActiveTransition();
    }

    @Bean
    public Transition<Action.Command> listCmdActiveTransition(FindTeacher findTeacher, StudentQuery studentQuery) {
        return new ListCmdActiveTransition(findTeacher, studentQuery);
    }

    @Bean
    public Transition<Action.Command> defaultCmdActiveTransition() {
        return new DefaultCmdActiveTransition();
    }

    @Bean
    public Transition<Action.Button> newStudentBtnActiveTransition() {
        return new NewStudentBtnActiveTransition();
    }

    @Bean
    public Transition<Action.Button> myStudentsBtnActiveTransition(FindTeacher findTeacher, StudentQuery studentQuery) {
        return new MyStudentsBtnActiveTransition(findTeacher, studentQuery);
    }

    // ---- AWAITING_REGISTRATION_NAME ----
    @Bean
    public Transition<Action.Input> inputAwaitingRegistrationNameTransition(RegisterTeacher registerTeacher) {
        return new InputAwaitingRegistrationNameTransition(registerTeacher);
    }

    // ---- AWAITING_STUDENT_NAME ----
    @Bean
    public Transition<Action.Input> inputAwaitingStudentNameTransition(FindTeacher findTeacher,
                                                                       CreateStudentWithDictionary createStudentWithDictionary) {
        return new InputAwaitingStudentNameTransition(findTeacher, createStudentWithDictionary);
    }

    // ---- STUDENTS_LIST ----
    @Bean
    public Transition<Action.Callback> studentCbStudentsListTransition(FindTeacher findTeacher, StudentQuery studentQuery) {
        return new StudentCbStudentsListTransition(findTeacher, studentQuery);
    }

    @Bean
    public Transition<Action.Callback> backCbStudentsListTransition() {
        return new BackCbStudentsListTransition();
    }

    @Bean
    public Transition<Action.Command> newCmdStudentsListTransition() {
        return new NewCmdStudentsListTransition();
    }

    @Bean
    public Transition<Action.Command> listCmdStudentsListTransition(FindTeacher findTeacher, StudentQuery studentQuery) {
        return new ListCmdStudentsListTransition(findTeacher, studentQuery);
    }

    // ---- STUDENT_OPTIONS ----
    @Bean
    public Transition<Action.Callback> actionCbStudentOptionsTransition(StartLesson startLesson,
                                                                        FindTeacher findTeacher,
                                                                        StudentQuery studentQuery) {
        return new ActionCbStudentOptionsTransition(startLesson, findTeacher, studentQuery);
    }

    // ---- STUDENT_DETAILS ----
    @Bean
    public Transition<Action.Callback> backCbStudentDetailsTransition(FindTeacher findTeacher, StudentQuery studentQuery) {
        return new BackCbStudentDetailsTransition(findTeacher, studentQuery);
    }

    // ---- IN_LESSON ----
    @Bean
    public Transition<Action> addWordInLessonTransition() {
        return new AddWordInLessonTransition();
    }

    @Bean
    public Transition<Action> finishLessonInLessonTransition(FindActiveLesson findActiveLesson,
                                                             EndLesson endLesson,
                                                             StudentQuery studentQuery,
                                                             FindWords findWords) {
        return new FinishLessonInLessonTransition(findActiveLesson, endLesson, studentQuery, findWords);
    }

    @Bean
    public Transition<Action.Command> helpCmdInLessonTransition() {
        return new HelpCmdInLessonTransition();
    }

    // ---- AWAITING_WORD / AWAITING_POS / AWAITING_TRANSLATION ----
    @Bean
    public Transition<Action.Input> inputAwaitingWordTransition() {
        return new InputAwaitingWordTransition();
    }

    @Bean
    public Transition<Action.Callback> posCbAwaitingPosTransition() {
        return new PosCbAwaitingPosTransition();
    }

    @Bean
    public Transition<Action.Input> inputAwaitingTranslationTransition(StudentQuery studentQuery,
                                                                       AddWordToDictionary addWordToDictionary,
                                                                       FindActiveLesson findActiveLesson,
                                                                       AddWordToLesson addWordToLesson) {
        return new InputAwaitingTranslationTransition(studentQuery, addWordToDictionary,
                findActiveLesson, addWordToLesson);
    }

    // ---- EXERCISE flow ----
    @Bean
    public Transition<Action.Callback> exerciseCbAwatingExerciseTypeTransition() {
        return new ExerciseCbAwatingExerciseTypeTransition();
    }

    @Bean
    public Transition<Action.Input> inputAwatingExerciseTopicTransition(StudentQuery studentQuery,
                                                                        GenerateExercise generateExercise) {
        return new InputAwatingExerciseTopicTransition(studentQuery, generateExercise);
    }

    @Bean
    public Transition<Action.Input> inputAwatingExerciseAnswerTransition(CheckExercise checkExercise) {
        return new InputAwatingExerciseAnswerTransition(checkExercise);
    }

    // ========================================================================
    // Registration
    // ========================================================================

    @Bean
    public StateMachine stateMachine(Map<ChatState, Handler> chatStateHandlerMap,
                                     ChatService chatService, FindTeacher findTeacher, StudentQuery studentQuery,
                                     FindActiveLesson findActiveLesson, EndLesson endLesson, FindWords findWords,
                                     StartLesson startLesson, CreateStudentWithDictionary createStudentWithDictionary,
                                     RegisterTeacher registerTeacher, AddWordToDictionary addWordToDictionary,
                                     AddWordToLesson addWordToLesson, GenerateExercise generateExercise, CheckExercise checkExercise) {
        StateMachine stateMachine = new StateMachine(chatStateHandlerMap, chatService);

        // INITIAL
        stateMachine.onCommand(INITIAL, "/register", registerCmdInitialTransition());
        stateMachine.onCommand(INITIAL, "/start", defaultCmdInitialTransition());
        stateMachine.onCommand(INITIAL, "/help", defaultCmdInitialTransition());

        // ACTIVE
        stateMachine.onCommand(ACTIVE, "/new", newCmdActiveTransition());
        stateMachine.onCommand(ACTIVE, "/list", listCmdActiveTransition(findTeacher, studentQuery));
        stateMachine.onCommand(ACTIVE, "/help", defaultCmdActiveTransition());
        stateMachine.onCommand(ACTIVE, "/start", defaultCmdActiveTransition());
        stateMachine.onButton(ACTIVE, Buttons.ADD_STUDENT.getValue(), newStudentBtnActiveTransition());
        stateMachine.onButton(ACTIVE, Buttons.LIST_STUDENT.getValue(), myStudentsBtnActiveTransition(findTeacher, studentQuery));

        // AWAITING_REGISTRATION_NAME
        stateMachine.onInput(AWAITING_REGISTRATION_NAME, inputAwaitingRegistrationNameTransition(registerTeacher));

        // AWAITING_STUDENT_NAME
        stateMachine.onInput(AWAITING_STUDENT_NAME, inputAwaitingStudentNameTransition(findTeacher, createStudentWithDictionary));

        // STUDENTS_LIST
        stateMachine.onCallback(STUDENTS_LIST, "student", studentCbStudentsListTransition(findTeacher, studentQuery));
        stateMachine.onCallback(STUDENTS_LIST, "back", backCbStudentsListTransition());
        stateMachine.onCommand(STUDENTS_LIST, "/new", newCmdStudentsListTransition());
        stateMachine.onCommand(STUDENTS_LIST, "/list", listCmdStudentsListTransition(findTeacher, studentQuery));
        stateMachine.onCommand(STUDENTS_LIST, "/help", listCmdStudentsListTransition(findTeacher, studentQuery));

        // STUDENT_OPTIONS
        stateMachine.onCallback(STUDENT_OPTIONS, "action", actionCbStudentOptionsTransition(startLesson, findTeacher, studentQuery));

        // STUDENT_DETAILS
        stateMachine.onCallback(STUDENT_DETAILS, "details", backCbStudentDetailsTransition(findTeacher, studentQuery));

        // IN_LESSON
        stateMachine.onButton(IN_LESSON, Buttons.ADD_WORD.getValue(), addWordInLessonTransition());
        stateMachine.onCommand(IN_LESSON, "/addword", addWordInLessonTransition());
        stateMachine.onButton(IN_LESSON, Buttons.FINISH_LESSON.getValue(), finishLessonInLessonTransition(findActiveLesson, endLesson, studentQuery, findWords));
        stateMachine.onCommand(IN_LESSON, "/finishlesson", finishLessonInLessonTransition(findActiveLesson, endLesson, studentQuery, findWords));
        stateMachine.onCommand(IN_LESSON, "/help", helpCmdInLessonTransition());

        // word flow: AWAITING_WORD -> AWAITING_POS -> AWAITING_TRANSLATION
        stateMachine.onInput(AWAITING_WORD, inputAwaitingWordTransition());
        stateMachine.onCallback(AWAITING_POS, "pos", posCbAwaitingPosTransition());
        stateMachine.onInput(AWAITING_TRANSLATION, inputAwaitingTranslationTransition(studentQuery, addWordToDictionary, findActiveLesson, addWordToLesson));

        // exercise flow: AWAITING_EXERCISE_TYPE -> AWAITING_EXERCISE_TOPIC -> AWAITING_EXERCISE_ANSWER
        stateMachine.onCallback(AWAITING_EXERCISE_TYPE, "exercise", exerciseCbAwatingExerciseTypeTransition());
        stateMachine.onInput(AWAITING_EXERCISE_TOPIC, inputAwatingExerciseTopicTransition(studentQuery, generateExercise));
        stateMachine.onInput(AWAITING_EXERCISE_ANSWER, inputAwatingExerciseAnswerTransition(checkExercise));

        if (!stateMachine.isReady()) {
            throw new RuntimeException();
        }

        return stateMachine;
    }

}
