package com.hydroyura.eta.chatbot.application.statemachine.transition.active;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;

public class DefaultCmdActiveTransition implements Transition<Action.Command> {

    private final ActiveMenuSupport activeMenuSupport;

    public DefaultCmdActiveTransition(ActiveMenuSupport activeMenuSupport) {
        this.activeMenuSupport = activeMenuSupport;
    }

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        return activeMenuSupport.render(chat);
    }

}
