package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_SEARCH;

/**
 * Текстовый ввод на экране выбора темы трактуется как поисковый запрос по каталогу.
 */
@RequiredArgsConstructor
public class InputAwaitingExerciseTopicTransition implements Transition<Action.Input> {

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var query = input.text() == null ? "" : input.text().trim();
        if (query.isEmpty()) {
            return new ActionResult.TextResponse(TOPIC_SEARCH);
        }
        return support.search(chat, query);
    }
}
