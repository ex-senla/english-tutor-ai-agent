package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

@Component
public class AwaitingRegistrationNameHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse("Введите ваше имя");
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_REGISTRATION_NAME;
    }

}
