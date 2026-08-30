package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

@Component
public class AwaitingStudentNameHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse("Введите имя ученика");
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_STUDENT_NAME;
    }
}
