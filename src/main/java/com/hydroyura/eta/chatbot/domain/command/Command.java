package com.hydroyura.eta.chatbot.domain.command;

import com.hydroyura.eta.chatbot.domain.statemachine.Context;

public interface Command {

    Type getType();

    CommandExecutionResult execute(Context context, String message);

}
