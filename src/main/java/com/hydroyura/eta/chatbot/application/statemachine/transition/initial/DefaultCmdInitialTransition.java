package com.hydroyura.eta.chatbot.application.statemachine.transition.initial;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;

import static com.hydroyura.eta.chatbot.view.Messages.WELCOME;

public class DefaultCmdInitialTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        return new ActionResult.TextResponse(WELCOME);
    }

}
