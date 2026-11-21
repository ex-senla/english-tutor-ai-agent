package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_TEACHERS_HINT;

@Component
public class AdminTeachersHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(ADMIN_TEACHERS_HINT);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.ADMIN_TEACHERS;
    }
}
