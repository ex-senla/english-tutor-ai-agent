package com.hydroyura.eta.chatbot.domain.inputchain;

import java.util.Objects;

public record ChainResult(Boolean isChainCompleted, String message) {

    public ChainResult {
        Objects.requireNonNull(message);
        Objects.requireNonNull(isChainCompleted);
    }
}
