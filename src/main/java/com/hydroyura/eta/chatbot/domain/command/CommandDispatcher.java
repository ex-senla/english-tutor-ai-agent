package com.hydroyura.eta.chatbot.domain.command;

public interface CommandDispatcher {

    Command dispatch(String message);
    Command get(Class<? extends Command> clazz);

}
