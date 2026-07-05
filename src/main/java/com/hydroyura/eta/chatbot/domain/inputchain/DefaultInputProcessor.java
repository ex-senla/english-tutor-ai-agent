package com.hydroyura.eta.chatbot.domain.inputchain;

import com.hydroyura.eta.chatbot.domain.statemachine.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DefaultInputProcessor implements InputProcessor {

    private final String failureMessage;
    private final String contextKey;
    private final Class<?> contextClass;

    @Override
    public boolean checkInput(Context context, String message) {
        var isInputCompleted = context.getSafely(contextKey, contextClass).isPresent();
        if (isInputCompleted) {
            return true;
        }

        if (isMessageCommand(message)) {
            return false;
        }
        context.put(contextKey, message);
        return true;
    }

    @Override
    public String getFailureMessage() {
        return failureMessage;
    }

    private Boolean isMessageCommand(String message) {
        return false;
    }

}
