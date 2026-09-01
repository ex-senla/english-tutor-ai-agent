package com.hydroyura.eta.chatbot.view.lesson;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;

import java.util.List;

import static com.hydroyura.eta.chatbot.view.util.ItemUtils.createCallbackData;

public class LessonView {

    public static ActionResult lessonKeyboard(String message) {
        return lessonKeyboard(message, null);
    }

    public static ActionResult lessonKeyboard(String message, Integer cleanupMessageId) {
        var keyboard = List.of(
                List.of(Buttons.ADD_WORD, Buttons.FINISH_LESSON)
        );
        return new ActionResult.TextWithReplyKeyboard(message, keyboard, cleanupMessageId);
    }

    public static ActionResult finishMenuWithKeyBoard(String date, long duration, List<String> wordValues, String studentName) {
        var summary = """
                🏁 Урок завершён!
                📅 Дата: %s
                ⏱ Длительность: %d мин.
                📝 Слова: %s
                👤 Ученик: %s""".formatted(date, duration, wordValues.isEmpty() ? "—" : String.join(", ", wordValues), studentName);

        var keyboard = List.of(
                List.of(new ActionResult.InlineButton(Buttons.START_LESSON, createCallbackData(Callbacks.ACTION, Callbacks.START_LESSON))),
                List.of(new ActionResult.InlineButton(Buttons.DETAILS, createCallbackData(Callbacks.ACTION, Callbacks.DETAILS))),
                List.of(new ActionResult.InlineButton(Buttons.EXERCISE, createCallbackData(Callbacks.ACTION, Callbacks.EXERCISE))),
                List.of(new ActionResult.InlineButton(Buttons.BACK, createCallbackData(Callbacks.ACTION, Callbacks.BACK)))
        );
        return new ActionResult.TextWithInlineKeyboard(summary, keyboard);
    }

}
