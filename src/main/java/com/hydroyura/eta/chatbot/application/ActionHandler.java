package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.action.ActionResult.InlineButton;
import com.hydroyura.eta.chatbot.domain.statemachine.State;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.student.api.lesson.AddWordToLesson;
import com.hydroyura.eta.student.api.lesson.AddWordToLessonCommand;
import com.hydroyura.eta.student.api.lesson.EndLesson;
import com.hydroyura.eta.student.api.lesson.EndLessonCommand;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.StartLesson;
import com.hydroyura.eta.student.api.lesson.StartLessonCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import java.util.Set;
import java.util.UUID;
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
    private final FindActiveLesson findActiveLesson;
    private final StartLesson startLesson;
    private final EndLesson endLesson;
    private final AddWordToDictionary addWordToDictionary;
    private final AddWordToLesson addWordToLesson;

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
                case "/new" -> {
                    sm.updateState(State.AWAITING_STUDENT_NAME);
                    yield new ActionResult.TextResponse("Введите имя нового ученика");
                }
                case "/list" -> {
                    sm.updateState(State.STUDENTS_LIST);
                    yield studentsListMenu(sm);
                }
                case "/help", "/start" -> activeMenu(userName);
                default -> new ActionResult.TextResponse(
                        "Неизвестная команда. /help — список команд");
            };
        }
        // input param or callback in ACTIVE - handle button taps
        if (action instanceof Action.InputParam(var text)) {
            if ("➕ Новый студент".equals(text)) {
                sm.updateState(State.AWAITING_STUDENT_NAME);
                return new ActionResult.TextResponse("Введите имя нового ученика");
            }
            if ("👥 Мои студенты".equals(text)) {
                sm.updateState(State.STUDENTS_LIST);
                return studentsListMenu(sm);
            }
            return new ActionResult.TextResponse("/help для списка команд");
        }
        return new ActionResult.TextResponse("Неизвестная команда. /help — список команд");
    }

    private ActionResult activeMenu(String userName) {
        return activeMenu(userName, null);
    }

    private ActionResult activeMenu(String userName, Integer cleanupMessageId) {
        var text = activeMenuText(userName);
        var keyboard = List.of(
                List.of("➕ Новый студент", "👥 Мои студенты")
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard, cleanupMessageId);
    }

    private String activeMenuText(String userName) {
        return "Главное меню\n\n" +
                "➕ Новый студент — добавить ученика\n" +
                "👥 Мои студенты — список учеников";
    }

    private ActionResult activeMenuWithMessage(String message, String userName) {
        var text = message + "\n\n" + activeMenuText(userName);
        var keyboard = List.of(
                List.of("➕ Новый студент", "👥 Мои студенты")
        );
        return new ActionResult.TextWithReplyKeyboard(text, keyboard, null);
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
        if (action instanceof Action.Callback(var data, var messageId)) {
            if (data.startsWith("student:")) {
                var studentId = data.substring("student:".length());
                sm.getContext().put("selectedStudentId", studentId);
                sm.updateState(State.STUDENT_OPTIONS);
                return studentOptionsEdit(sm, studentId, messageId);
            }
            if ("back:main".equals(data)) {
                sm.updateState(State.ACTIVE);
                var teacherName = (String) sm.getContext().getOrDefault("teacherName", "");
                return activeMenu(teacherName, messageId);
            }
            return new ActionResult.TextResponse("Выберите ученика кнопками ниже");
        }
        if (action instanceof Action.Command(var cmd, var userName)) {
            if ("/new".equals(cmd)) {
                sm.updateState(State.AWAITING_STUDENT_NAME);
                return new ActionResult.TextResponse("Введите имя нового ученика");
            }
            if ("/list".equals(cmd) || "/help".equals(cmd)) {
                return studentsListMenu(sm);
            }
        }
        return new ActionResult.TextResponse("Выберите ученика кнопками ниже");
    }

    private ActionResult studentsListMenu(StateMachine sm) {
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());

        if (studentIds.isEmpty()) {
            return new ActionResult.TextResponse(
                    "У вас пока нет учеников.\n➕ Новый студент — добавить ученика");
        }

        var students = studentQuery.findStudentsByIds(studentIds);
        var keyboard = new java.util.ArrayList<>(students.stream()
                .map(s -> List.of(new InlineButton(s.name(), "student:" + s.id().value())))
                .toList());
        keyboard.add(List.of(new InlineButton("◀ Назад", "back:main")));

        return new ActionResult.TextWithInlineKeyboard("Ваши ученики:", keyboard);
    }

    // ========================================================================
    // S6: STUDENT_OPTIONS
    // ========================================================================

    private ActionResult handleStudentOptions(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data, var messageId)) {
            return switch (data) {
                case "action:startlesson" -> doStartLesson(sm, messageId);
                case "action:details" -> {
                    sm.updateState(State.STUDENT_DETAILS);
                    yield new ActionResult.EditMessageText(messageId, "Детали ученика (TODO)",
                            List.of(List.of(new ActionResult.InlineButton("◀ Back", "details:back"))));
                }
                case "action:exercise" -> {
                    sm.updateState(State.AWAITING_EXERCISE_TYPE);
                    yield exerciseTypeEdit(messageId);
                }
                case "action:back" -> {
                    sm.updateState(State.STUDENTS_LIST);
                    yield studentsListEdit(sm, messageId);
                }
                default -> new ActionResult.TextResponse("Используйте кнопки ниже");
            };
        }
        return new ActionResult.TextResponse("Используйте кнопки ниже");
    }

    private ActionResult doStartLesson(StateMachine sm, int messageId) {
        var studentId = (String) sm.getContext().get("selectedStudentId");
        var sid = new StudentId(UUID.fromString(studentId));
        var name = (String) sm.getContext().getOrDefault("selectedStudentName", "?");

        var lessonId = startLesson.execute(new StartLessonCommand(sid, "Урок " + name));
        sm.getContext().put("activeLessonId", lessonId.value().toString());
        sm.updateState(State.IN_LESSON);
        log.info("Lesson {} started for student {}", lessonId, studentId);

        return lessonKeyboard("Урок начат для " + name + "!", messageId);
    }

    private ActionResult lessonKeyboard(String message) {
        return lessonKeyboard(message, null);
    }

    private ActionResult lessonKeyboard(String message, Integer cleanupMessageId) {
        var keyboard = List.of(
                List.of("➕ Добавить слово", "🏁 Завершить урок")
        );
        return new ActionResult.TextWithReplyKeyboard(message, keyboard, cleanupMessageId);
    }

    private ActionResult studentOptionsMenu(StateMachine sm, String studentId) {
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());
        var name = studentQuery.findStudentsByIds(studentIds).stream()
                .filter(s -> s.id().value().toString().equals(studentId))
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
        if (action instanceof Action.Callback(var data, var messageId)) {
            if ("details:back".equals(data)) {
                sm.updateState(State.STUDENT_OPTIONS);
                var studentId = (String) sm.getContext().get("selectedStudentId");
                return studentOptionsEdit(sm, studentId, messageId);
            }
        }
        return new ActionResult.TextResponse("Используйте кнопки ниже");
    }

    // ========================================================================
    // S8: IN_LESSON
    // ========================================================================

    private ActionResult handleInLesson(StateMachine sm, Action action) {
        if (action instanceof Action.InputParam(var text)) {
            if ("➕ Добавить слово".equals(text)) {
                sm.updateState(State.AWAITING_WORD);
                return new ActionResult.TextResponse("Введите слово на английском");
            }
            if ("🏁 Завершить урок".equals(text)) {
                return finishLesson(sm);
            }
        }
        // backward compatibility: commands still work
        if (action instanceof Action.Command(var cmd, var userName)) {
            if ("/finishlesson".equals(cmd)) {
                return finishLesson(sm);
            }
            if ("/addword".equals(cmd)) {
                sm.updateState(State.AWAITING_WORD);
                return new ActionResult.TextResponse("Введите слово на английском");
            }
            if ("/help".equals(cmd)) {
                return lessonKeyboard("➕ Добавить слово — добавить слово\n🏁 Завершить урок — завершить урок");
            }
        }
        return lessonKeyboard("Используйте кнопки ниже");
    }

    private ActionResult finishLesson(StateMachine sm) {
        var studentIdStr = (String) sm.getContext().get("selectedStudentId");
        var studentId = new StudentId(UUID.fromString(studentIdStr));

        var lessonId = findActiveLesson.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalStateException("No active lesson for student " + studentIdStr));

        endLesson.execute(new EndLessonCommand(lessonId));
        log.info("Lesson {} ended for student {}", lessonId, studentIdStr);

        sm.updateState(State.STUDENT_OPTIONS);
        return studentOptionsMenu(sm, studentIdStr);
    }

    // ========================================================================
    // S9-S11: Word input
    // ========================================================================

    private ActionResult handleAwaitingWord(StateMachine sm, Action action) {
        if (action instanceof Action.InputParam(var text)) {
            sm.getContext().put("wordValue", text);
            sm.updateState(State.AWAITING_POS);
            return posMenu(text);
        }
        return new ActionResult.TextResponse("Введите слово на английском");
    }

    private ActionResult handleAwaitingPos(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data, var messageId)) {
            if (data.startsWith("pos:")) {
                var pos = PartOfSpeech.valueOf(data.substring("pos:".length()));
                sm.getContext().put("wordPos", pos);
                sm.updateState(State.AWAITING_TRANSLATION);
                var word = (String) sm.getContext().get("wordValue");
                var posLabel = switch (pos) {
                    case NOUN -> "📛 Noun";
                    case VERB -> "🏃 Verb";
                    case ADJECTIVE -> "🎨 Adjective";
                };
                return new ActionResult.EditMessageText(messageId,
                        "Слово: " + word + "\nЧасть речи: " + posLabel
                                + "\n\nВведите переводы через запятую (например: дом, здание, строение)", List.of());
            }
        }
        return new ActionResult.TextResponse("Выберите часть речи кнопками ниже");
    }

    private ActionResult handleAwaitingTranslation(StateMachine sm, Action action) {
        if (action instanceof Action.InputParam(var text)) {
            var studentIdStr = (String) sm.getContext().get("selectedStudentId");
            var studentId = new StudentId(UUID.fromString(studentIdStr));
            var wordValue = (String) sm.getContext().get("wordValue");
            var pos = (PartOfSpeech) sm.getContext().get("wordPos");

            var translations = Set.of(text.split("\\s*,\\s*"));

            var dictionaryId = studentQuery.getDictionaryId(studentId)
                    .orElseThrow(() -> new IllegalStateException("No dictionary for student " + studentIdStr));

            var wordId = addWordToDictionary.execute(
                    new AddWordCommand(dictionaryId, wordValue, translations, pos));

            var lessonId = findActiveLesson.findByStudentId(studentId)
                    .orElseThrow(() -> new IllegalStateException("No active lesson for student " + studentIdStr));

            addWordToLesson.execute(new AddWordToLessonCommand(lessonId, wordId));
            log.info("Word '{}' added to lesson {} for student {}", wordValue, lessonId, studentIdStr);

            sm.updateState(State.IN_LESSON);
            var posLabel = switch (pos) {
                case NOUN -> "📛 Noun";
                case VERB -> "🏃 Verb";
                case ADJECTIVE -> "🎨 Adjective";
            };
            return lessonKeyboard("✅ Слово '" + wordValue + "' (" + posLabel + ")\nПереводы: " + String.join(", ", translations));
        }
        return new ActionResult.TextResponse("Введите переводы через запятую");
    }

    private ActionResult posMenu(String word) {
        var keyboard = List.of(
                List.of(new InlineButton("📛 Noun", "pos:NOUN")),
                List.of(new InlineButton("🏃 Verb", "pos:VERB")),
                List.of(new InlineButton("🎨 Adjective", "pos:ADJECTIVE"))
        );
        return new ActionResult.TextWithInlineKeyboard("Слово: " + word + "\n\nВыберите часть речи:", keyboard);
    }

    // ========================================================================
    // S12: AWAITING_EXERCISE_TYPE
    // ========================================================================

    private ActionResult handleAwaitingExerciseType(StateMachine sm, Action action) {
        if (action instanceof Action.Callback(var data, var messageId)) {
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

    // ========================================================================
    // EDIT message helpers (replace existing inline keyboard message)
    // ========================================================================

    private ActionResult studentOptionsEdit(StateMachine sm, String studentId, int messageId) {
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());
        var name = studentQuery.findStudentsByIds(studentIds).stream()
                .filter(s -> s.id().value().toString().equals(studentId))
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
        return new ActionResult.EditMessageText(messageId, "Ученик: " + name, keyboard);
    }

    private ActionResult studentsListEdit(StateMachine sm, int messageId) {
        var studentIds = findTeacher.getStudentIds(sm.getId().chatId());

        if (studentIds.isEmpty()) {
            return new ActionResult.EditMessageText(messageId,
                    "У вас пока нет учеников.\n➕ Новый студент — добавить ученика", List.of());
        }

        var students = studentQuery.findStudentsByIds(studentIds);
        var keyboard = new java.util.ArrayList<>(students.stream()
                .map(s -> List.of(new InlineButton(s.name(), "student:" + s.id().value())))
                .toList());
        keyboard.add(List.of(new InlineButton("◀ Назад", "back:main")));

        return new ActionResult.EditMessageText(messageId, "Ваши ученики:", keyboard);
    }

    private ActionResult exerciseTypeEdit(int messageId) {
        var keyboard = List.of(
                List.of(new InlineButton("✏️ Fill in the blank", "exercise:FILL_IN_THE_BLANK")),
                List.of(new InlineButton("🔤 Multiple choice", "exercise:MULTIPLE_CHOICE"))
        );
        return new ActionResult.EditMessageText(messageId, "Выберите тип упражнения:", keyboard);
    }
}
