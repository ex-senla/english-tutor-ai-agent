package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.chat.ChatState;

public record TransitionKey(ChatState initialState, String trigger) {
}
