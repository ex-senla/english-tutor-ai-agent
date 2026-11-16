package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisethematictopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.exercise.TopicSelectionView;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.EXERCISE_THEMATIC_TOPIC;
import static com.hydroyura.eta.chatbot.view.Messages.THEMATIC_TOPIC_EMPTY;
import static com.hydroyura.eta.chatbot.view.Messages.THEMATIC_TOPIC_TOO_LONG;

/**
 * Ввод тематической темы: свободный текст без каталога и LLM-валидации.
 * Пустой/слишком длинный ввод остаётся на шаге; иначе тематика сохраняется
 * и флоу переходит к выбору слов.
 */
@RequiredArgsConstructor
public class InputAwaitingExerciseThematicTopicTransition implements Transition<Action.Input> {

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var raw = input.text() == null ? "" : input.text();
        var trimmed = raw.trim();

        if (trimmed.isEmpty()) {
            return TopicSelectionView.thematicPrompt(THEMATIC_TOPIC_EMPTY);
        }
        if (trimmed.length() > TopicFlowSupport.MAX_TOPIC_LENGTH) {
            return TopicSelectionView.thematicPrompt(THEMATIC_TOPIC_TOO_LONG);
        }

        chat.getContext().put(EXERCISE_THEMATIC_TOPIC.getValue(), trimmed);
        return support.enterWordSelection(chat);
    }
}
