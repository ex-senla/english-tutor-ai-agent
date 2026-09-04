package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.springframework.stereotype.Component;

import static com.hydroyura.eta.chatbot.view.Messages.CHOOSE_EXERCISE_TYPE;

@Component
public class AwatingExerciseTypeHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return new ActionResult.TextResponse(CHOOSE_EXERCISE_TYPE);
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_EXERCISE_TYPE;
    }
}
