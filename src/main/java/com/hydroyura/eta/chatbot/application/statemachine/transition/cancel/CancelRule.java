package com.hydroyura.eta.chatbot.application.statemachine.transition.cancel;

import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.domain.chat.ContextKey;
import java.util.List;

/**
 * Правило отмены текущего флоу: целевое состояние и ключи контекста, которые нужно удалить.
 */
public record CancelRule(ChatState target, List<ContextKey> keysToRemove) {
}
