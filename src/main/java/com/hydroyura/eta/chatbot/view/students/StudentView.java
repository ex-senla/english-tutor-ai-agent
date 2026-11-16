package com.hydroyura.eta.chatbot.view.students;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentInfo;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.hydroyura.eta.chatbot.view.Messages.NO_STUDENTS;
import static com.hydroyura.eta.chatbot.view.Messages.SEARCH_NOTHING_FOUND;
import static com.hydroyura.eta.chatbot.view.Messages.SEARCH_PROMPT;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_DELETE_CONFIRM;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_DELETED;
import static com.hydroyura.eta.chatbot.view.Messages.STUDENT_NOT_FOUND_OR_DELETED;
import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

@RequiredArgsConstructor
public class StudentView {

    public static ActionResult studentsListMenu(List<StudentInfo> students) {
        if (students.isEmpty()) {
            return new ActionResult.TextResponse(NO_STUDENTS);
        }

        var keyboard = new ArrayList<>(students.stream()
                .map(s -> List.of(new ActionResult.InlineButton(s.name(), createCallbackData(Callbacks.STUDENT, s.id()
                        .value().toString()))))
                .toList());
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.BACK,
                Callbacks.MAIN))));

        return new ActionResult.TextWithInlineKeyboard("Ваши ученики:", keyboard);
    }

    public static ActionResult searchPrompt() {
        return new ActionResult.TextWithInlineKeyboard(SEARCH_PROMPT, List.of(backButton()));
    }

    public static ActionResult searchResults(List<StudentInfo> students, int limit) {
        var keyboard = new ArrayList<>(students.stream()
                .limit(limit)
                .map(s -> List.of(new ActionResult.InlineButton(s.name(), createCallbackData(Callbacks.STUDENT, s.id()
                        .value().toString()))))
                .toList());
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.BACK,
                Callbacks.MAIN))));
        return new ActionResult.TextWithInlineKeyboard("Найдено:", keyboard);
    }

    public static ActionResult searchNothingFound() {
        return new ActionResult.TextWithInlineKeyboard(SEARCH_NOTHING_FOUND, List.of(backButton()));
    }

    private static List<ActionResult.InlineButton> backButton() {
        return List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.BACK, Callbacks.MAIN)));
    }

    public static List<List<ActionResult.InlineButton>> optionsKeyboard() {
        return List.of(
                List.of(new ActionResult.InlineButton(Buttons.START_LESSON, createCallbackData(Callbacks.ACTION,
                        Callbacks.START_LESSON))),
                List.of(new ActionResult.InlineButton(Buttons.DETAILS, createCallbackData(Callbacks.ACTION,
                        Callbacks.DETAILS))),
                List.of(new ActionResult.InlineButton(Buttons.DICTIONARY, createCallbackData(Callbacks.ACTION,
                        Callbacks.DICTIONARY))),
                List.of(new ActionResult.InlineButton(Buttons.EXERCISE, createCallbackData(Callbacks.ACTION,
                        Callbacks.EXERCISE))),
                List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.ACTION,
                        Callbacks.BACK)))
        );
    }

    public static ActionResult options(int messageId, String name) {
        return new ActionResult.EditMessageText(messageId, "Ученик: " + name, optionsKeyboard());
    }

    public static ActionResult optionsFresh(String name) {
        return new ActionResult.TextWithInlineKeyboard("Ученик: " + name, optionsKeyboard());
    }

    public static ActionResult listEdit(int messageId, List<StudentInfo> students) {
        return new ActionResult.EditMessageText(messageId, "Ваши ученики:", studentsListKeyboard(students));
    }

    public static ActionResult deletedStudentsList(int messageId, List<StudentInfo> students) {
        if (students.isEmpty()) {
            return new ActionResult.EditMessageText(messageId, NO_STUDENTS, List.of());
        }
        return new ActionResult.EditMessageText(messageId, STUDENT_DELETED, studentsListKeyboard(students));
    }

    public static ActionResult studentNotFoundOrDeleted(int messageId, List<StudentInfo> students) {
        if (students.isEmpty()) {
            return new ActionResult.EditMessageText(messageId, NO_STUDENTS, List.of());
        }
        return new ActionResult.EditMessageText(messageId, STUDENT_NOT_FOUND_OR_DELETED, studentsListKeyboard(
                students));
    }

    public static ActionResult deleteConfirmation(int messageId, String name) {
        return new ActionResult.EditMessageText(messageId, STUDENT_DELETE_CONFIRM.formatted(name), List.of(
                List.of(new ActionResult.InlineButton(Buttons.DELETE_CONFIRM,
                        createCallbackData(Callbacks.STUDENT_DELETE_CONFIRM))),
                List.of(new ActionResult.InlineButton(Buttons.DELETE_CANCEL,
                        createCallbackData(Callbacks.BACK, Callbacks.MAIN)))
        ));
    }

    public static ActionResult studentDetails(int messageId, StudentDetails details) {
        return new ActionResult.EditMessageText(messageId, studentDetailsText(details), detailsKeyboard());
    }

    public static ActionResult studentDetailsFresh(StudentDetails details) {
        return new ActionResult.TextWithInlineKeyboard(studentDetailsText(details), detailsKeyboard());
    }

    private static String studentDetailsText(StudentDetails details) {
        return """
                🧑‍🎓 Ученик: %s
                📊 Уровень: %s

                📚 Словарь · всего %d
                ├ 🆕 Новые: %d
                ├ 🔄 В процессе: %d
                └ ✅ Выучено: %d%s""".formatted(
                details.name(),
                details.level().code(),
                details.dictionaryStats().totalWords(),
                details.dictionaryStats().newCount(),
                details.dictionaryStats().inProgressCount(),
                details.dictionaryStats().learnedCount(),
                details.hasActiveLesson() ? "\n\n▶️ Активный урок: есть" : ""
        );
    }

    private static List<List<ActionResult.InlineButton>> detailsKeyboard() {
        return List.of(
                List.of(new ActionResult.InlineButton(Buttons.CHANGE_LEVEL,
                        createCallbackData(Callbacks.LEVEL_CHANGE))),
                List.of(new ActionResult.InlineButton(Buttons.DELETE_STUDENT,
                        createCallbackData(Callbacks.STUDENT_DELETE))),
                List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.DETAILS,
                        Callbacks.BACK)))
        );
    }

    private static List<List<ActionResult.InlineButton>> studentsListKeyboard(List<StudentInfo> students) {
        var keyboard = new ArrayList<>(students.stream()
                .map(info -> List.of(new ActionResult.InlineButton(info.name(),
                        createCallbackData(Callbacks.STUDENT, info.id().value().toString()))))
                .toList());
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK,
                createCallbackData(Callbacks.BACK, Callbacks.MAIN))));
        return keyboard;
    }

    public static List<List<ActionResult.InlineButton>> levelKeyboard() {
        return List.of(
                List.of(levelButton(CefrLevel.A1), levelButton(CefrLevel.A2), levelButton(CefrLevel.B1)),
                List.of(levelButton(CefrLevel.B2), levelButton(CefrLevel.C1), levelButton(CefrLevel.C2))
        );
    }

    public static List<List<ActionResult.InlineButton>> levelKeyboardWithBack() {
        var keyboard = new ArrayList<List<ActionResult.InlineButton>>(levelKeyboard());
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.BACK,
                Callbacks.MAIN))));
        return keyboard;
    }

    private static ActionResult.InlineButton levelButton(CefrLevel level) {
        return new ActionResult.InlineButton(level.code(), createCallbackData(Callbacks.LEVEL, level.code()));
    }

}
