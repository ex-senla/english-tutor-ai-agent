package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopicconfirm;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisetopic.TopicFlowSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.exercise.TopicSelectionView;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_CANONICAL_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.TOPIC_MATCHED_SLUG;

/**
 * Кнопки подтверждения темы: «✓ Использовать» / «✍️ Ввести заново» / «🔙 Назад».
 * Регистрируется также в {@code AWAITING_CUSTOM_TOPIC} для кнопок невалидного ввода.
 */
@RequiredArgsConstructor
public class TopicConfirmCbAwaitingExerciseTopicConfirmTransition implements Transition<Action.Callback> {

    private final TopicFlowSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return switch (callback.payload()) {
            case Callbacks.TOPIC_CONFIRM_USE -> use(chat);
            case Callbacks.TOPIC_CONFIRM_REENTER -> reenter(chat, callback);
            case Callbacks.TOPIC_CONFIRM_BACK -> back(chat, callback);
            default -> back(chat, callback);
        };
    }

    private ActionResult use(Chat chat) {
        var canonical = (String) chat.getContext().get(TOPIC_CANONICAL_NAME.getValue());
        if (canonical == null || canonical.isBlank()) {
            chat.updateState(ChatState.AWAITING_CUSTOM_TOPIC);
            return TopicSelectionView.selfInputFresh();
        }
        chat.getContext().remove(TOPIC_CANONICAL_NAME.getValue());
        chat.getContext().remove(TOPIC_MATCHED_SLUG.getValue());
        return support.enterThematicTopicStep(chat, new CustomTopic(canonical));
    }

    private ActionResult reenter(Chat chat, Action.Callback callback) {
        chat.getContext().remove(TOPIC_CANONICAL_NAME.getValue());
        chat.getContext().remove(TOPIC_MATCHED_SLUG.getValue());
        chat.updateState(ChatState.AWAITING_CUSTOM_TOPIC);
        return TopicSelectionView.selfInput(callback.messageId());
    }

    private ActionResult back(Chat chat, Action.Callback callback) {
        chat.getContext().remove(TOPIC_CANONICAL_NAME.getValue());
        chat.getContext().remove(TOPIC_MATCHED_SLUG.getValue());
        return support.backToTopicSelection(chat, callback.messageId());
    }
}
