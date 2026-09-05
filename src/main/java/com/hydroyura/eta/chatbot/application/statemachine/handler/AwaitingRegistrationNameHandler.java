package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_YOUR_NAME;

@Component
public class AwaitingRegistrationNameHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(ENTER_YOUR_NAME);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_REGISTRATION_NAME;
    }

}
