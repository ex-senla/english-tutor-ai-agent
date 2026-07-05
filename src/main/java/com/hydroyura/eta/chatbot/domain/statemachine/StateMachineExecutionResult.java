package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.List;
import java.util.Objects;

public record StateMachineExecutionResult(String message, List<Object> inlineButtons, List<Object> replyButtons) {

    public StateMachineExecutionResult {
        if (Objects.isNull(message) || Objects.isNull(inlineButtons) || Objects.isNull(replyButtons)) {
            throw new RuntimeException();
        }

        // TODO: other validations
    }
}
