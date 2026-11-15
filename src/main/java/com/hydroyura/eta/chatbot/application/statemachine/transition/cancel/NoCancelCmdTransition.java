package com.hydroyura.eta.chatbot.application.statemachine.transition.cancel;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;

import static com.hydroyura.eta.chatbot.view.Messages.NOTHING_TO_CANCEL;

public class NoCancelCmdTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        return new ActionResult.TextResponse(NOTHING_TO_CANCEL);
    }
}
