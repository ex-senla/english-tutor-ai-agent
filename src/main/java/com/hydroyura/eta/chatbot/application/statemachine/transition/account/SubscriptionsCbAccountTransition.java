package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.account.AccountView;

public class SubscriptionsCbAccountTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return AccountView.subscriptionsStub();
    }
}
