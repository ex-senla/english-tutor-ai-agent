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
import static com.hydroyura.eta.chatbot.view.util.ItemUtils.createCallbackData;

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

    public static List<List<ActionResult.InlineButton>> optionsKeyboard() {
        return List.of(
                List.of(new ActionResult.InlineButton(Buttons.START_LESSON, createCallbackData(Callbacks.ACTION,
                        Callbacks.START_LESSON))),
                List.of(new ActionResult.InlineButton(Buttons.DETAILS, createCallbackData(Callbacks.ACTION,
                        Callbacks.DETAILS))),
                List.of(new ActionResult.InlineButton(Buttons.EXERCISE, createCallbackData(Callbacks.ACTION,
                        Callbacks.EXERCISE))),
                List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.ACTION,
                        Callbacks.BACK)))
        );
    }

    public static ActionResult options(int messageId, String name) {
        return new ActionResult.EditMessageText(messageId, "Ученик: " + name, optionsKeyboard());
    }

    public static ActionResult listEdit(int messageId, List<StudentInfo> students) {
        var keyboard = new java.util.ArrayList<>(students.stream()
                .map(s -> List.of(new ActionResult.InlineButton(s.name(), createCallbackData(Callbacks.STUDENT, s.id()
                        .value().toString()))))
                .toList());
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.BACK,
                Callbacks.MAIN))));

        return new ActionResult.EditMessageText(messageId, "Ваши ученики:", keyboard);
    }

    public static ActionResult studentDetails(int messageId, StudentDetails details) {
        var text = "Ученик: " + details.name()
                + "\nУровень: " + details.level().code()
                + "\n\nСлова: всего " + details.dictionaryStats().totalWords()
                + ", новые " + details.dictionaryStats().newCount()
                + ", в изучении " + details.dictionaryStats().inProgressCount()
                + ", изучены " + details.dictionaryStats().learnedCount()
                + (details.hasActiveLesson() ? "\n\nАктивный урок: есть" : "");

        var keyboard = List.of(
                List.of(new ActionResult.InlineButton(Buttons.CHANGE_LEVEL,
                        createCallbackData(Callbacks.LEVEL_CHANGE))),
                List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.DETAILS,
                        Callbacks.BACK)))
        );

        return new ActionResult.EditMessageText(messageId, text, keyboard);
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
