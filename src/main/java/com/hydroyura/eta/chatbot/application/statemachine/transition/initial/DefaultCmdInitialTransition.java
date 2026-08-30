package com.hydroyura.eta.chatbot.application.statemachine.transition.initial;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;

public class DefaultCmdInitialTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        return new ActionResult.TextResponse(
                "Добро пожаловать! Для начала зарегистрируйтесь: /register");
    }

    @Override
    public String getName() {
        return "DefaultCmdInitialTransition";
    }
}
