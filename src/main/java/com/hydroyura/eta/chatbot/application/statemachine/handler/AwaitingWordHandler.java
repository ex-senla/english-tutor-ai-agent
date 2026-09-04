package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_WORD_IN_ENGLISH;

@Component
public class AwaitingWordHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(ENTER_WORD_IN_ENGLISH);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_WORD;
    }
}
