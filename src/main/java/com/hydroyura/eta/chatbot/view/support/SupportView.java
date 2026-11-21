package com.hydroyura.eta.chatbot.view.support;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportMessageInfo;

import java.util.List;

import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

public final class SupportView {

    private SupportView() {
    }

    public static ActionResult thread(List<SupportMessageInfo> messages) {
        return new ActionResult.TextWithInlineKeyboard(history(messages, Messages.SUPPORT_ENTER),
                backKeyboard());
    }

    public static ActionResult sent(List<SupportMessageInfo> messages) {
        return new ActionResult.TextWithInlineKeyboard(history(messages, Messages.SUPPORT_SENT),
                backKeyboard());
    }

    private static String history(List<SupportMessageInfo> messages, String header) {
        var builder = new StringBuilder(header);
        if (messages.isEmpty()) {
            builder.append("\n\nИстория пуста.");
        } else {
            messages.forEach(message -> builder.append("\n\n")
                    .append(message.author() == SupportAuthor.TEACHER ? "👨‍🏫 Вы" : "🛡 Поддержка")
                    .append(": ")
                    .append(message.text()));
        }
        return builder.toString();
    }

    private static List<List<ActionResult.InlineButton>> backKeyboard() {
        return List.of(List.of(new ActionResult.InlineButton("🔙 Назад",
                createCallbackData(Callbacks.SUPPORT, Callbacks.BACK))));
    }
}
