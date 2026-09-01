package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

@Component
public class StudentOptionsHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(USE_BUTTONS_BELOW);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.STUDENT_OPTIONS;
    }
}
