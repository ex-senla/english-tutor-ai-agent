package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_SELF_INPUT;

@Component
public class AwaitingCustomTopicHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(TOPIC_SELF_INPUT);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_CUSTOM_TOPIC;
    }
}
