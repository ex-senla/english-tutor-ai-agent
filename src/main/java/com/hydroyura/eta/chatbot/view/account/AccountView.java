package com.hydroyura.eta.chatbot.view.account;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.Messages;

import java.util.List;

import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

public final class AccountView {

    private AccountView() {
    }

    public static ActionResult profile(String name, int activeStudentCount) {
        return new ActionResult.TextWithInlineKeyboard(
                Messages.ACCOUNT_PROFILE.formatted(name, activeStudentCount),
                profileKeyboard());
    }

    public static ActionResult subscriptionsStub() {
        return new ActionResult.TextResponse(Messages.SUBSCRIPTIONS_STUB);
    }

    private static List<List<ActionResult.InlineButton>> profileKeyboard() {
        return List.of(
                List.of(button("💬 Поддержка", Callbacks.SUPPORT)),
                List.of(button("💳 Подписки", Callbacks.SUBSCRIPTIONS)),
                List.of(button("📖 Справка", Callbacks.ONBOARDING)),
                List.of(button("🔙 Назад", Callbacks.BACK)));
    }

    private static ActionResult.InlineButton button(String label, String payload) {
        return new ActionResult.InlineButton(label, createCallbackData(Callbacks.ACCOUNT, payload));
    }
}
