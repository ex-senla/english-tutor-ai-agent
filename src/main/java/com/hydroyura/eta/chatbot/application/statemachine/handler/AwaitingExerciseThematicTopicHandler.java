package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.exercise.TopicSelectionView;
import org.springframework.stereotype.Component;

@Component
public class AwaitingExerciseThematicTopicHandler implements Handler {

    @Override
    public ActionResult handle(Chat chat) {
        return TopicSelectionView.thematicPromptFresh();
    }

    @Override
    public ChatState getChatState() {
        return ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC;
    }
}
