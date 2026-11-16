package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisethematictopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;

/**
 * Кнопка «⏭ Пропустить»: тематика не сохраняется (ключ удаляется на случай мусора
 * от предыдущих сессий), флоу переходит к выбору слов.
 */
@RequiredArgsConstructor
public class SkipCbAwaitingExerciseThematicTopicTransition implements Transition<Action.Callback> {

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.getContext().remove(EXERCISE_THEMATIC_TOPIC.getValue());
        return support.enterWordSelection(chat);
    }
}
