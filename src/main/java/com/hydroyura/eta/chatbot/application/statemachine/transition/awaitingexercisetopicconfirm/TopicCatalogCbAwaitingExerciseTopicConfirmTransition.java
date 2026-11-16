package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopicconfirm;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;

/**
 * «📚 Использовать тему каталога» — использует найденную тему каталога вместо введённого текста.
 */
@RequiredArgsConstructor
public class TopicCatalogCbAwaitingExerciseTopicConfirmTransition implements Transition<Action.Callback> {

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.getContext().remove(TOPIC_CANONICAL_NAME.getValue());
        chat.getContext().remove(TOPIC_MATCHED_SLUG.getValue());
        return support.selectCatalogTopic(chat, callback.payload());
    }
}
