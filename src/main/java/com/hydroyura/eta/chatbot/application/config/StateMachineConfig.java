package com.hydroyura.eta.chatbot.application.config;

import com.hydroyura.eta.chatbot.application.config.properties.ChatbotPaginationProperties;
import com.hydroyura.eta.chatbot.application.config.properties.ChatbotSearchProperties;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.application.statemachine.handler.Handler;
import com.hydroyura.eta.chatbot.application.statemachine.transition.NewStudentTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.DefaultCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.active.NewCmdActiveTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexerciseanswer.InputAwaitingExerciseAnswerTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisegrammar.InputAwaitingExerciseGrammarTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.InputAwaitingExerciseTopicTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetype.ExerciseCbAwaitingExerciseTypeTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingmanualtranslation.InputAwaitingManualTranslationTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos.PosCbAwaitingPosTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingregistrationname.InputAwaitingRegistrationNameTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevel.LevelCbAwaitingStudentLevelTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange.BackCbAwaitingStudentLevelChangeTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange.LevelCbAwaitingStudentLevelChangeTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentname.InputAwaitingStudentNameTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslationchoice.TranslationCbAwaitingTranslationChoiceTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.cancel.CancelCmdTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.cancel.CancelRule;
import com.hydroyura.eta.chatbot.application.statemachine.transition.cancel.CancelScreenRenderer;
import com.hydroyura.eta.chatbot.application.statemachine.transition.cancel.NoCancelCmdTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.initial.DefaultCmdInitialTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.initial.RegisterCmdInitialTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.FinishLessonInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.HelpCmdInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson.InputInLessonTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails.BackCbStudentDetailsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails.LevelChangeCbStudentDetailsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentoptions.ActionCbStudentOptionsTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch.BackCbStudentSearchTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch.InputStudentSearchTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch.StudentCbStudentSearchTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch.StudentSearchTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.BackCbStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.ListCmdStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist.StudentCbStudentsListTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.AlertBackCbAwaitingExerciseWordSelectionTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.ExerciseWordSelectionSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.FilterCbAwaitingExerciseWordSelectionTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.GenerateCbAwaitingExerciseWordSelectionTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.PageCbAwaitingExerciseWordSelectionTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.SelectionBackCbAwaitingExerciseWordSelectionTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection.WordToggleCbAwaitingExerciseWordSelectionTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary.BackCbStudentDictionaryTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary.PageCbStudentDictionaryTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary.ResetCbStudentDictionaryTransition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.studentdictionary.StudentDictionarySupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.word.LessonWordSupport;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Commands;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.chatbot.view.menu.MenuView;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.student.ChangeStudentLevel;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hydroyura.eta.chatbot.domain.chat.ChatState.ACTIVE;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_ANSWER;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_GRAMMAR;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_EXERCISE_WORD_SELECTION;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_MANUAL_TRANSLATION;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_REGISTRATION_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_STUDENT_LEVEL;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_STUDENT_LEVEL_CHANGE;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.AWAITING_TRANSLATION_CHOICE;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.IN_LESSON;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.INITIAL;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENT_DETAILS;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENT_DICTIONARY;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENT_OPTIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENT_SEARCH;
import static com.hydroyura.eta.chatbot.domain.chat.ChatState.STUDENTS_LIST;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TOPIC;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_TYPE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.DICTIONARY_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.GRAMMAR_RULE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.PENDING_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTION_PAGE;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_FILTER;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_SELECTION;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static com.hydroyura.eta.chatbot.view.Messages.LESSON_INSTRUCTION;
import static com.hydroyura.eta.chatbot.view.Messages.WELCOME;

@Configuration
@EnableConfigurationProperties({ChatbotSearchProperties.class,
        ChatbotPaginationProperties.class})
public class StateMachineConfig {

    @Bean
    public Map<ChatState, Handler> chatStateHandlerMap(List<Handler> handlers) {
        return handlers.stream().collect(Collectors.toMap(Handler::getChatState, Function.identity()));
    }

    @Bean
    public StateMachine stateMachine(Map<ChatState, Handler> chatStateHandlerMap, FindTeacher findTeacher,
            StudentQuery studentQuery, FindActiveLesson findActiveLesson, EndLesson endLesson, FindWords findWords,
            StartLesson startLesson, CreateStudentWithDictionary createStudentWithDictionary,
            RegisterTeacher registerTeacher, AddWordToDictionary addWordToDictionary, AddWordToLesson addWordToLesson,
            ChangeStudentLevel changeStudentLevel, GenerateExercise generateExercise, CheckExercise checkExercise,
            GenerateExercisePdf generateExercisePdf, CheckWord checkWord, WordTranslator wordTranslator,
            ResetWordLearning resetWordLearning, ChatbotSearchProperties chatbotSearchProperties,
            ChatbotPaginationProperties chatbotPaginationProperties) {
        StateMachine stateMachine = new StateMachine(chatStateHandlerMap);

        // переходы, регистрируемые под несколькими ключами
        var defaultCmdInitialTransition = new DefaultCmdInitialTransition();
        var defaultCmdActiveTransition = new DefaultCmdActiveTransition();
        var studentListTransition = new StudentListTransition(findTeacher, studentQuery);
        var newStudentTransition = new NewStudentTransition();
        var listCmdStudentsListTransition = new ListCmdStudentsListTransition(findTeacher, studentQuery);
        var finishLessonInLessonTransition = new FinishLessonInLessonTransition(findActiveLesson, endLesson,
                studentQuery, findWords);
        var studentSearchTransition = new StudentSearchTransition();
        var lessonWordSupport = new LessonWordSupport(studentQuery, addWordToDictionary, findActiveLesson,
                addWordToLesson);

        // INITIAL
        stateMachine.onCommand(INITIAL, Commands.REGISTER, new RegisterCmdInitialTransition());
        stateMachine.onCommand(INITIAL, Commands.START, defaultCmdInitialTransition);
        stateMachine.onCommand(INITIAL, Commands.HELP, defaultCmdInitialTransition);

        // ACTIVE
        stateMachine.onCommand(ACTIVE, Commands.NEW, new NewCmdActiveTransition());
        stateMachine.onCommand(ACTIVE, Commands.LIST, studentListTransition);
        stateMachine.onCommand(ACTIVE, Commands.HELP, defaultCmdActiveTransition);
        stateMachine.onCommand(ACTIVE, Commands.START, defaultCmdActiveTransition);
        stateMachine.onCommand(ACTIVE, Commands.FIND, studentSearchTransition);
        stateMachine.onButton(ACTIVE, Buttons.NEW_STUDENT, newStudentTransition);
        stateMachine.onButton(ACTIVE, Buttons.LIST_STUDENT, studentListTransition);
        stateMachine.onButton(ACTIVE, Buttons.FIND_STUDENT, studentSearchTransition);

        // AWAITING_REGISTRATION_NAME
        stateMachine.onInput(AWAITING_REGISTRATION_NAME, new InputAwaitingRegistrationNameTransition(registerTeacher));

        // AWAITING_STUDENT_NAME
        stateMachine.onInput(AWAITING_STUDENT_NAME, new InputAwaitingStudentNameTransition());

        // AWAITING_STUDENT_LEVEL
        stateMachine.onCallback(AWAITING_STUDENT_LEVEL, Callbacks.LEVEL,
                new LevelCbAwaitingStudentLevelTransition(findTeacher, createStudentWithDictionary));

        // STUDENTS_LIST
        stateMachine.onCallback(STUDENTS_LIST, Callbacks.STUDENT, new StudentCbStudentsListTransition(findTeacher,
                studentQuery));
        stateMachine.onCallback(STUDENTS_LIST, Callbacks.BACK, new BackCbStudentsListTransition());
        stateMachine.onCommand(STUDENTS_LIST, Commands.NEW, newStudentTransition);
        stateMachine.onCommand(STUDENTS_LIST, Commands.LIST, listCmdStudentsListTransition);
        stateMachine.onCommand(STUDENTS_LIST, Commands.HELP, listCmdStudentsListTransition);

        // STUDENT_OPTIONS
        stateMachine.onCallback(STUDENT_OPTIONS, Callbacks.ACTION, new ActionCbStudentOptionsTransition(startLesson,
                findTeacher, studentQuery, findWords, chatbotPaginationProperties));

        // STUDENT_DETAILS
        stateMachine.onCallback(STUDENT_DETAILS, Callbacks.DETAILS, new BackCbStudentDetailsTransition(findTeacher,
                studentQuery));
        stateMachine.onCallback(STUDENT_DETAILS, Callbacks.LEVEL_CHANGE, new LevelChangeCbStudentDetailsTransition());

        // AWAITING_STUDENT_LEVEL_CHANGE
        stateMachine.onCallback(AWAITING_STUDENT_LEVEL_CHANGE, Callbacks.LEVEL,
                new LevelCbAwaitingStudentLevelChangeTransition(studentQuery, changeStudentLevel));
        stateMachine.onCallback(AWAITING_STUDENT_LEVEL_CHANGE, Callbacks.BACK,
                new BackCbAwaitingStudentLevelChangeTransition(studentQuery));

        // IN_LESSON
        stateMachine.onInput(IN_LESSON, new InputInLessonTransition(checkWord));
        stateMachine.onButton(IN_LESSON, Buttons.FINISH_LESSON, finishLessonInLessonTransition);
        stateMachine.onCommand(IN_LESSON, Commands.FINISH_LESSON, finishLessonInLessonTransition);
        stateMachine.onCommand(IN_LESSON, Commands.HELP, new HelpCmdInLessonTransition());

        // word flow: IN_LESSON -> AWAITING_POS -> AWAITING_TRANSLATION_CHOICE / AWAITING_MANUAL_TRANSLATION
        stateMachine.onCallback(AWAITING_POS, Callbacks.POS, new PosCbAwaitingPosTransition(wordTranslator));
        stateMachine.onCallback(AWAITING_TRANSLATION_CHOICE, Callbacks.TRANSLATION,
                new TranslationCbAwaitingTranslationChoiceTransition(lessonWordSupport));
        stateMachine.onInput(AWAITING_MANUAL_TRANSLATION, new InputAwaitingManualTranslationTransition(
                lessonWordSupport));

        // student search
        stateMachine.onInput(STUDENT_SEARCH, new InputStudentSearchTransition(findTeacher, studentQuery,
                chatbotSearchProperties));
        stateMachine.onCallback(STUDENT_SEARCH, Callbacks.STUDENT, new StudentCbStudentSearchTransition(findTeacher,
                studentQuery));
        stateMachine.onCallback(STUDENT_SEARCH, Callbacks.BACK, new BackCbStudentSearchTransition());

        // exercise flow: AWAITING_EXERCISE_TYPE -> AWAITING_EXERCISE_GRAMMAR -> AWAITING_EXERCISE_TOPIC -> AWAITING_EXERCISE_ANSWER
        stateMachine.onCallback(AWAITING_EXERCISE_TYPE, Callbacks.EXERCISE,
                new ExerciseCbAwaitingExerciseTypeTransition());
        stateMachine.onInput(AWAITING_EXERCISE_GRAMMAR, new InputAwaitingExerciseGrammarTransition());
        stateMachine.onInput(AWAITING_EXERCISE_TOPIC, new InputAwaitingExerciseTopicTransition(studentQuery,
                findWords, chatbotPaginationProperties));
        stateMachine.onInput(AWAITING_EXERCISE_ANSWER, new InputAwaitingExerciseAnswerTransition(checkExercise));

        // STUDENT_DICTIONARY
        var studentDictionarySupport = new StudentDictionarySupport(studentQuery, findWords,
                chatbotPaginationProperties);
        stateMachine.onCallback(STUDENT_DICTIONARY, Callbacks.WORD_DICTIONARY_PAGE,
                new PageCbStudentDictionaryTransition(studentDictionarySupport));
        stateMachine.onCallback(STUDENT_DICTIONARY, Callbacks.WORD_RESET,
                new ResetCbStudentDictionaryTransition(studentQuery, resetWordLearning, studentDictionarySupport));
        stateMachine.onCallback(STUDENT_DICTIONARY, Callbacks.WORD_DICTIONARY_BACK,
                new BackCbStudentDictionaryTransition(studentQuery));

        // AWAITING_EXERCISE_WORD_SELECTION
        var exerciseWordSelectionSupport = new ExerciseWordSelectionSupport(studentQuery, findWords,
                chatbotPaginationProperties);
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD,
                new WordToggleCbAwaitingExerciseWordSelectionTransition(exerciseWordSelectionSupport));
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD_FILTER,
                new FilterCbAwaitingExerciseWordSelectionTransition(exerciseWordSelectionSupport));
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD_PAGE,
                new PageCbAwaitingExerciseWordSelectionTransition(exerciseWordSelectionSupport));
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD_DONE,
                new GenerateCbAwaitingExerciseWordSelectionTransition(true, exerciseWordSelectionSupport,
                        generateExercise, generateExercisePdf));
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD_CONTINUE,
                new GenerateCbAwaitingExerciseWordSelectionTransition(false, exerciseWordSelectionSupport,
                        generateExercise, generateExercisePdf));
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD_ALERT_BACK,
                new AlertBackCbAwaitingExerciseWordSelectionTransition(exerciseWordSelectionSupport));
        stateMachine.onCallback(AWAITING_EXERCISE_WORD_SELECTION, Callbacks.EXERCISE_WORD_SELECTION_BACK,
                new SelectionBackCbAwaitingExerciseWordSelectionTransition());

        registerCancelTransitions(stateMachine, studentQuery);

        if (!stateMachine.isReady()) {
            throw new RuntimeException();
        }

        return stateMachine;
    }

    private void registerCancelTransitions(StateMachine stateMachine, StudentQuery studentQuery) {
        var noCancel = new NoCancelCmdTransition();

        var initialRenderer = (CancelScreenRenderer) chat -> new ActionResult.TextResponse(WELCOME);
        var activeRenderer = (CancelScreenRenderer) chat -> MenuView.activeMenu();
        var lessonRenderer = (CancelScreenRenderer) chat -> LessonView.lessonKeyboard(LESSON_INSTRUCTION);
        var studentOptionsRenderer = (CancelScreenRenderer) chat -> StudentView.optionsFresh(
                (String) chat.getContext().getOrDefault(SELECTED_STUDENT_NAME.getValue(), "?"));
        var studentDetailsRenderer = (CancelScreenRenderer) chat -> StudentSupport.studentDetailsFresh(studentQuery,
                chat);

        var renderers = Map.of(
                INITIAL, initialRenderer,
                ACTIVE, activeRenderer,
                IN_LESSON, lessonRenderer,
                STUDENT_OPTIONS, studentOptionsRenderer,
                STUDENT_DETAILS, studentDetailsRenderer);

        var cancelRules = new EnumMap<ChatState, CancelRule>(ChatState.class);
        cancelRules.put(AWAITING_REGISTRATION_NAME, new CancelRule(INITIAL, List.of()));
        cancelRules.put(AWAITING_STUDENT_NAME, new CancelRule(ACTIVE, List.of(PENDING_STUDENT_NAME)));
        cancelRules.put(AWAITING_STUDENT_LEVEL, new CancelRule(ACTIVE, List.of(PENDING_STUDENT_NAME)));
        cancelRules.put(AWAITING_STUDENT_LEVEL_CHANGE, new CancelRule(STUDENT_DETAILS, List.of()));
        cancelRules.put(AWAITING_POS, new CancelRule(IN_LESSON, List.of(WORD_VALUE)));
        cancelRules.put(AWAITING_TRANSLATION_CHOICE,
                new CancelRule(IN_LESSON, List.of(WORD_VALUE, WORD_POS, WORD_TRANSLATIONS)));
        cancelRules.put(AWAITING_MANUAL_TRANSLATION,
                new CancelRule(IN_LESSON, List.of(WORD_VALUE, WORD_POS, WORD_TRANSLATIONS)));
        cancelRules.put(STUDENT_SEARCH, new CancelRule(ACTIVE, List.of()));
        cancelRules.put(AWAITING_EXERCISE_TYPE, new CancelRule(STUDENT_OPTIONS, List.of()));
        cancelRules.put(AWAITING_EXERCISE_GRAMMAR, new CancelRule(STUDENT_OPTIONS, List.of(EXERCISE_TYPE)));
        cancelRules.put(AWAITING_EXERCISE_TOPIC, new CancelRule(STUDENT_OPTIONS, List.of(EXERCISE_TYPE, GRAMMAR_RULE)));
        cancelRules.put(AWAITING_EXERCISE_WORD_SELECTION,
                new CancelRule(STUDENT_OPTIONS, List.of(EXERCISE_TYPE, GRAMMAR_RULE, EXERCISE_TOPIC, WORD_SELECTION,
                        WORD_FILTER, SELECTION_PAGE)));
        cancelRules.put(AWAITING_EXERCISE_ANSWER,
                new CancelRule(STUDENT_OPTIONS, List.of(EXERCISE_ID, EXERCISE_TOPIC, EXERCISE_TYPE)));
        cancelRules.put(STUDENT_DICTIONARY, new CancelRule(STUDENT_DETAILS, List.of(DICTIONARY_PAGE)));

        cancelRules.forEach((state, rule) -> stateMachine.onCommand(state, Commands.CANCEL,
                new CancelCmdTransition(rule, renderers.get(rule.target()))));

        for (var state : List.of(INITIAL, ACTIVE, STUDENTS_LIST, STUDENT_OPTIONS, STUDENT_DETAILS, IN_LESSON)) {
            stateMachine.onCommand(state, Commands.CANCEL, noCancel);
        }
    }
}
