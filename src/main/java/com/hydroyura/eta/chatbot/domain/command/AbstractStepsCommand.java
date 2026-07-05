package com.hydroyura.eta.chatbot.domain.command;

import com.hydroyura.eta.chatbot.domain.inputchain.Chain;
import com.hydroyura.eta.chatbot.domain.inputchain.ChainResult;
import com.hydroyura.eta.chatbot.domain.statemachine.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractStepsCommand implements Command {

    private final Chain chain;


    @Override
    public CommandExecutionResult execute(Context context, String message) {
        var chainResult = chain.checkInput(context, message);
        return convertResult(chainResult);
    }

    protected CommandExecutionResult convertResult(ChainResult chainResult) {
        return null;
    }

}
