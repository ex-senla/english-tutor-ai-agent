package com.hydroyura.eta.chatbot.item.lesson;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;

import java.util.List;

public class LessonItem {

    public static ActionResult lessonKeyboard(String message) {
        return lessonKeyboard(message, null);
    }

    public static ActionResult lessonKeyboard(String message, Integer cleanupMessageId) {
        var keyboard = List.of(
                List.of("➕ Добавить слово", "🏁 Завершить урок")
        );
        return new ActionResult.TextWithReplyKeyboard(message, keyboard, cleanupMessageId);
    }

    public static ActionResult finishMenuWithKeyBoard(String date, long duration, List<String> wordValues, String studentName) {
        var summary = "🏁 Урок завершён!\n" +
                "📅 Дата: " + date + "\n" +
                "⏱ Длительность: " + duration + " мин.\n" +
                "📝 Слова: " + (wordValues.isEmpty() ? "—" : String.join(", ", wordValues)) + "\n" +
                "👤 Ученик: " + studentName;

        var keyboard = List.of(
                List.of(new ActionResult.InlineButton("▶ Start Lesson", "action:startlesson")),
                List.of(new ActionResult.InlineButton("📋 Details", "action:details")),
                List.of(new ActionResult.InlineButton("🎯 Exercise", "action:exercise")),
                List.of(new ActionResult.InlineButton("◀ Back", "action:back"))
        );
        return new ActionResult.TextWithInlineKeyboard(summary, keyboard);
    }

}
