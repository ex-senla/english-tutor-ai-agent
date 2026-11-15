package com.hydroyura.eta.chatbot.application.statemachine.transition.cancel;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;

/**
 * Рендерит целевой экран после отмены (без текста подтверждения).
 */
@FunctionalInterface
public interface CancelScreenRenderer {

    ActionResult render(Chat chat);
}
