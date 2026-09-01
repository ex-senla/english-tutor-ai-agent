package com.hydroyura.eta.chatbot.application.statemachine.transition.initial;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_YOUR_NAME;

public class RegisterCmdInitialTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        chat.updateState(ChatState.AWAITING_REGISTRATION_NAME);
        return new ActionResult.TextResponse(ENTER_YOUR_NAME);
    }

}
