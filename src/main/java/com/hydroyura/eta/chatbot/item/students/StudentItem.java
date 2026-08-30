package com.hydroyura.eta.chatbot.item.students;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.student.api.student.StudentInfo;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class StudentItem {

    public static ActionResult studentsListMenu(Chat chat, List<StudentInfo> students) {
        if (students.isEmpty()) {
            return new ActionResult.TextResponse(
                    "У вас пока нет учеников.\n➕ Новый студент — добавить ученика");
        }

        var keyboard = new ArrayList<>(students.stream()
                .map(s -> List.of(new ActionResult.InlineButton(s.name(), "student:" + s.id().value())))
                .toList());
        keyboard.add(List.of(new ActionResult.InlineButton("◀ Назад", "back:main")));

        return new ActionResult.TextWithInlineKeyboard("Ваши ученики:", keyboard);
    }

    public static List<List<ActionResult.InlineButton>> optionsKeyboard() {
        return List.of(
                List.of(new ActionResult.InlineButton("▶ Start Lesson", "action:startlesson")),
                List.of(new ActionResult.InlineButton("📋 Details", "action:details")),
                List.of(new ActionResult.InlineButton("🎯 Exercise", "action:exercise")),
                List.of(new ActionResult.InlineButton("◀ Back", "action:back"))
        );
    }

    public static ActionResult options(int messageId, String name) {
        return new ActionResult.EditMessageText(messageId, "Ученик: " + name, optionsKeyboard());
    }

    public static ActionResult listEdit(int messageId, List<StudentInfo> students) {
        var keyboard = new java.util.ArrayList<>(students.stream()
                .map(s -> List.of(new ActionResult.InlineButton(s.name(), "student:" + s.id().value())))
                .toList());
        keyboard.add(List.of(new ActionResult.InlineButton("◀ Назад", "back:main")));

        return new ActionResult.EditMessageText(messageId, "Ваши ученики:", keyboard);
    }
}
