package com.hydroyura.eta.chatbot.domain.inputchain;

import com.hydroyura.eta.chatbot.domain.statemachine.Context;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class Chain {

    private final List<InputProcessor> processors;
    private final String successMessage;


    public ChainResult checkInput(Context context, String message) {
        for (InputProcessor processor: processors) {
            var processorResult = processor.checkInput(context, message);

            if (!processorResult) {
                return new ChainResult(Boolean.FALSE, processor.getFailureMessage());
            }
        }

        return new ChainResult(Boolean.TRUE, successMessage);
    }


}
