package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

@Component
public class InitialHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(
                "Добро пожаловать! Для начала зарегистрируйтесь: /register");
    }

    @Override
    public ChatState getChatState() {
        return ChatState.INITIAL;
    }
}
