package com.hydroyura.eta.chatbot.view.students;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
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

    public static ActionResult studentDetails(int messageId) {
        return new ActionResult.EditMessageText(messageId, "Детали ученика (TODO)",
                List.of(List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.DETAILS,
                        Callbacks.BACK)))));
    }

}
