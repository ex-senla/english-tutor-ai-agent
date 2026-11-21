package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.Callbacks;

import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

public class AccountCbTransition implements Transition<Action.Callback> {

    private final SupportCbAccountTransition support;

    private final SubscriptionsCbAccountTransition subscriptions;

    private final BackCbAccountTransition back;

    public AccountCbTransition(SupportCbAccountTransition support, SubscriptionsCbAccountTransition subscriptions,
            BackCbAccountTransition back) {
        this.support = support;
        this.subscriptions = subscriptions;
        this.back = back;
    }

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return switch (callback.payload()) {
            case Callbacks.SUPPORT -> support.transit(chat, callback);
            case Callbacks.SUBSCRIPTIONS -> subscriptions.transit(chat, callback);
            case Callbacks.BACK -> back.transit(chat, callback);
            default -> new ActionResult.TextResponse(USE_BUTTONS_BELOW);
        };
    }
}
