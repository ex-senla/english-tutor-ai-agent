package com.hydroyura.eta.chatbot.application.statemachine.transition.account;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackCbAccountTransition implements Transition<Action.Callback> {

    private final ActiveMenuSupport activeMenuSupport;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.updateState(ChatState.ACTIVE);
        return activeMenuSupport.render(chat, callback.messageId());
    }
}
