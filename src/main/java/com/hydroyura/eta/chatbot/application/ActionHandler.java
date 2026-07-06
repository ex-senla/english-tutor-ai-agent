package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.action.ActionResult.InlineButton;
import com.hydroyura.eta.chatbot.domain.statemachine.State;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionHandler {

    private final FindTeacher findTeacher;
    private final RegisterTeacher registerTeacher;
    private final CreateStudentWithDictionary createStudentWithDictionary;
    private final StudentQuery studentQuery;

    public ActionResult handle(StateMachine sm, Action action) {
        return switch (sm.getState()) {
            case INITIAL                    -> handleInitial(sm, action);
            case AWAITING_REGISTRATION_NAME -> handleAwaitingRegistrationName(sm, action);
            case ACTIVE                     -> handleActive(sm, action);
            case AWAITING_STUDENT_NAME      -> handleAwaitingStudentName(sm, action);
            case STUDENTS_LIST              -> handleStudentsList(sm, action);
            case STUDENT_OPTIONS            -> handleStudentOptions(sm, action);
            case STUDENT_DETAILS            -> handleStudentDetails(sm, action);
            case IN_LESSON                  -> handleInLesson(sm, action);
            case AWAITING_WORD              -> handleAwaitingWord(sm, action);
            case AWAITING_POS               -> handleAwaitingPos(sm, action);
            case AWAITING_TRANSLATION       -> handleAwaitingTranslation(sm, action);
            case AWAITING_EXERCISE_TYPE     -> handleAwaitingExerciseType(sm, action);
            case AWAITING_EXERCISE_TOPIC    -> handleAwaitingExerciseTopic(sm, action);
            case AWAITING_EXERCISE_ANSWER   -> handleAwaitingExerciseAnswer(sm, action);
        };
    }

    // ========================================================================
    // S1: INITIAL
    // ========================================================================

    private ActionResult handleInitial(StateMachine sm, Action action) {
        if (action instanceof Action.Command(var cmd, var userName)) {
            if ("/register".equals(cmd)) {
                sm.updateState(State.AWAITING_REGISTRATION_NAME);
                return new ActionResult.TextResponse("Введите ваше имя");
            }
            if ("/start".equals(cmd) || "/help".equals(cmd)) {
                return new ActionResult.TextResponse(
                        "Добро пожаловать! Для начала зарегистрируйтесь: /register");
            }
        }
        return new ActionResult.TextResponse(
                "Добро пожаловать! Для начала зарегистрируйтесь: /register");
    }

    // ========================================================================
    // S2: AWAITING_REGISTRATION_NAME
    // ========================================================================

    private ActionResult handleAwaitingRegistrationName(StateMachine sm, Action action) {
        if (action instanceof Action.InputParam(var name)) {
            registerTeacher.execute(new RegisterTeacherCommand(sm.getId().chatId(), name));
            sm.getContext().put("teacherName", name);
            sm.updateState(State.ACTIVE);
            log.info("Teacher registered: chatId={}, name={}", sm.getId().chatId(), name);
            return activeMenu(name);
        }
        if (action instanceof Action.Command) {
            return new ActionResult.TextResponse("Введите ваше имя");
        }
        return new ActionResult.TextResponse("Введите ваше имя");
    }

    // ========================================================================
    // S3: ACTIVE — главное меню
    // ========================================================================

    private ActionResult handleActive(StateMachine sm, Action action) {
        if (action instanceof Action.Command(var cmd, var userName)) {
            return switch (cmd) {
                case "/newstudent" -> {
                    sm.updateState(State.AWAITING_STUDENT_NAME);
                    yield new ActionResult.TextResponse("Введите имя нового ученика");
                }
                case "/students" -> {
                    sm.updateState(State.STUDENTS_LIST);
                    yield studentsListMenu(sm);
                }
                case "/help", "/start" -> activeMenu(userName);
                default -> new ActionResult.TextResponse(
                        "Неизвестная команда. /help — список команд");
            };
        }
        // input param or callback in ACTIVE is an error
        if (action instanceof Action.InputParam) {
            return new ActionResult.TextResponse("/help для списка команд");
        }
        return new ActionResult.TextResponse("Неизвестная команда. /help — список команд");
    }

    private ActionResult activeMenu(String userName) {
        var text = activeMenuText(userName);
        var keyboard = List.of(
                List.of("/newstudent", "/students")
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard);
    }

    private String activeMenuText(String userName) {
        return "Главное меню\n\n" +
                "/newstudent — добавить ученика\n" +
                "/students — список учеников";
    }

    private ActionResult activeMenuWithMessage(String message, String userName) {
        var text = message + "\n\n" + activeMenuText(userName);
        var keyboard = List.of(
                List.of("/newstudent", "/students")
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard);
    }

    // ========================================================================
    // S4: AWAITING_STUDENT_NAME
    // ========================================================================

    private ActionResult handleAwaitingStudentName(StateMachine sm, Action action) {
        if (action instanceof Action.InputParam(var name)) {
            var teacherId = findTeacher.findByTelegramChatId(sm.getId().chatId())
                    .orElseThrow(() -> new IllegalStateException("Teacher not found for chatId=" + sm.getId().chatId()));

            try {
                var dictionaryName = "Словарь " + name;
                var studentId = createStudentWithDictionary.execute(
                        new CreateStudentWithDictionaryCommand(teacherId, name, dictionaryName));
                log.info("Student created: name={}, id={}, teacherId={}", name, studentId, teacherId);
                sm.updateState(State.ACTIVE);
                var teacherName = (String) sm.getContext().getOrDefault("teacherName", "");
                return activeMenuWithMessage("✅ Ученик '" + name + "' добавлен!", teacherName);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to create student '{}': {}", name, e.getMessage());
                return new ActionResult.TextResponse("❌ " + e.getMessage() + ". Введите другое имя:");
            }
        }
        if (action instanceof Action.Command) {
            return new ActionResult.TextResponse("Введите имя ученика");
        }
        return new ActionResult.TextResponse("Введите имя ученика");
    }

    // ========================================================================
    // S5: STUDENTS_LIST
    // ========================================================================

    private ActionResult handleStudentsList(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data)) {
            if (data.startsWith("student:")) {
                var studentId = data.substring("student:".length());
                sm.getContext().put("selectedStudentId", studentId);
                sm.updateState(State.STUDENT_OPTIONS);
                return studentOptionsMenu(sm, studentId);
            }
            return new ActionResult.TextResponse("Выберите ученика кнопками ниже");
        }
        if (action instanceof Action.Command(var cmd, var userName)) {
            if ("/newstudent".equals(cmd)) {
                sm.updateState(State.AWAITING_STUDENT_NAME);
                return new ActionResult.TextResponse("Введите имя нового ученика");
            }
            if ("/students".equals(cmd) || "/help".equals(cmd)) {
                return studentsListMenu(sm);
            }
        }
        return new ActionResult.TextResponse("Выберите ученика кнопками ниже");
    }

    private ActionResult studentsListMenu(StateMachine sm) {
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());

        if (studentIds.isEmpty()) {
            return new ActionResult.TextResponse(
                    "У вас пока нет учеников.\n/newstudent — добавить ученика");
        }

        var students = studentQuery.findStudentsByIds(studentIds);
        var keyboard = students.stream()
                .map(s -> List.of(new InlineButton(s.name(), "student:" + s.id().value())))
                .toList();

        return new ActionResult.TextWithInlineKeyboard("Ваши ученики:", keyboard);
    }

    // ========================================================================
    // S6: STUDENT_OPTIONS
    // ========================================================================

    private ActionResult handleStudentOptions(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data)) {
            var studentId = (String) sm.getContext().get("selectedStudentId");
            return switch (data) {
                case "action:startlesson" -> {
                    // TODO: StartLesson use case, store lessonId in context
                    sm.updateState(State.IN_LESSON);
                    yield new ActionResult.TextResponse("Урок начат! /addword — добавить слово, /finishlesson — завершить");
                }
                case "action:details" -> {
                    sm.updateState(State.STUDENT_DETAILS);
                    yield new ActionResult.TextResponse("Детали ученика (TODO)");
                }
                case "action:exercise" -> {
                    sm.updateState(State.AWAITING_EXERCISE_TYPE);
                    yield exerciseTypeMenu();
                }
                case "action:back" -> {
                    sm.updateState(State.STUDENTS_LIST);
                    yield studentsListMenu(sm);
                }
                default -> new ActionResult.TextResponse("Используйте кнопки ниже");
            };
        }
        return new ActionResult.TextResponse("Используйте кнопки ниже");
    }

    private ActionResult studentOptionsMenu(StateMachine sm, String studentId) {
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());
        var name = studentQuery.findStudentsByIds(studentIds).stream()
                .filter(s -> s.id().value().equals(studentId))
                .map(StudentInfo::name)
                .findFirst()
                .orElse("?");

        sm.getContext().put("selectedStudentName", name);

        var keyboard = List.of(
                List.of(new InlineButton("▶ Start Lesson", "action:startlesson")),
                List.of(new InlineButton("📋 Details", "action:details")),
                List.of(new InlineButton("🎯 Exercise", "action:exercise")),
                List.of(new InlineButton("◀ Back", "action:back"))
        );
        return new ActionResult.TextWithInlineKeyboard("Ученик: " + name, keyboard);
    }

    // ========================================================================
    // S7: STUDENT_DETAILS
    // ========================================================================

    private ActionResult handleStudentDetails(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data)) {
            if ("details:back".equals(data)) {
                sm.updateState(State.STUDENT_OPTIONS);
                var studentId = (String) sm.getContext().get("selectedStudentId");
                return studentOptionsMenu(sm, studentId);
            }
        }
        return new ActionResult.TextResponse("Используйте кнопки ниже");
    }

    // ========================================================================
    // S8: IN_LESSON
    // ========================================================================

    private ActionResult handleInLesson(StateMachine sm, Action action) {
        // TODO: implement
        return new ActionResult.TextResponse("Идёт урок (TODO)");
    }

    // ========================================================================
    // S9-S11: Word input
    // ========================================================================

    private ActionResult handleAwaitingWord(StateMachine sm, Action action) {
        return new ActionResult.TextResponse("Ввод слова (TODO)");
    }

    private ActionResult handleAwaitingPos(StateMachine sm, Action action) {
        return new ActionResult.TextResponse("Выбор части речи (TODO)");
    }

    private ActionResult handleAwaitingTranslation(StateMachine sm, Action action) {
        return new ActionResult.TextResponse("Ввод перевода (TODO)");
    }

    // ========================================================================
    // S12: AWAITING_EXERCISE_TYPE
    // ========================================================================

    private ActionResult handleAwaitingExerciseType(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data)) {
            if (data.equals("exercise:FILL_IN_THE_BLANK") || data.equals("exercise:MULTIPLE_CHOICE")) {
                sm.getContext().put("exerciseType", data);
                sm.updateState(State.AWAITING_EXERCISE_TOPIC);
                return new ActionResult.TextResponse("Введите тему упражнения (например, 'Animals')");
            }
        }
        return new ActionResult.TextResponse("Выберите тип упражнения кнопками ниже");
    }

    private ActionResult exerciseTypeMenu() {
        var keyboard = List.of(
                List.of(new InlineButton("✏️ Fill in the blank", "exercise:FILL_IN_THE_BLANK")),
                List.of(new InlineButton("🔤 Multiple choice", "exercise:MULTIPLE_CHOICE"))
        );
        return new ActionResult.TextWithInlineKeyboard("Выберите тип упражнения:", keyboard);
    }

    private ActionResult handleAwaitingExerciseTopic(StateMachine sm, Action action) {
        return new ActionResult.TextResponse("Ввод темы упражнения (TODO)");
    }

    private ActionResult handleAwaitingExerciseAnswer(StateMachine sm, Action action) {
        return new ActionResult.TextResponse("Проверка ответа (TODO)");
    }
}
