package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingcustomtopic;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.exercise.TopicSelectionView;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopic;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopicCommand;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_EMPTY;
import static com.hydroyura.eta.chatbot.view.Messages.TOPIC_TOO_LONG;

/**
 * Ввод «Своей темы»: детерминированные пре-проверки → LLM-валидация → подтверждение / fail-open.
 */
@RequiredArgsConstructor
public class InputAwaitingCustomTopicTransition implements Transition<Action.Input> {

    private final ValidateCustomTopic validateCustomTopic;

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var raw = input.text() == null ? "" : input.text();
        var trimmed = raw.trim();

        if (trimmed.isEmpty()) {
            return TopicSelectionView.customPrompt(TOPIC_EMPTY);
        }
        if (trimmed.length() > TopicFlowSupport.MAX_TOPIC_LENGTH) {
            return TopicSelectionView.customPrompt(TOPIC_TOO_LONG);
        }

        var result = validateCustomTopic.validate(new ValidateCustomTopicCommand(trimmed));

        if (!result.validated()) {
            return support.enterThematicTopicStep(chat, new CustomTopic(trimmed));
        }

        if (!result.valid()) {
            return TopicSelectionView.invalid(result.reason() == null ? "" : result.reason());
        }

        chat.getContext().put(TOPIC_CANONICAL_NAME.getValue(), result.canonicalName());
        chat.getContext().put(TOPIC_MATCHED_SLUG.getValue(), result.matchedSlug());
        chat.updateState(ChatState.AWAITING_EXERCISE_TOPIC_CONFIRM);
        return TopicSelectionView.confirm(result.canonicalName(), result.matchedSlug() != null,
                result.matchedSlug());
    }
}
