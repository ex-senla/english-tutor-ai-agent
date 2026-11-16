package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TopicCbAwaitingExerciseTopicTransition implements Transition<Action.Callback> {

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return support.selectCatalogTopic(chat, callback.payload());
    }
}
