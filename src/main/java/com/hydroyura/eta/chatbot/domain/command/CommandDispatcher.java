package com.hydroyura.eta.chatbot.domain.command;

public interface CommandDispatcher {

    Command dispatch(String command);
    Command get(Class<? extends Command> clazz);
}
