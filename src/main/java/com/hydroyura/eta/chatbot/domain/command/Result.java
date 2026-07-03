package com.hydroyura.eta.chatbot.domain.command;

import com.hydroyura.eta.chatbot.domain.statemachine.CommandType;
import com.hydroyura.eta.chatbot.domain.statemachine.Context;
import com.hydroyura.eta.chatbot.domain.statemachine.State;

import java.util.List;
import java.util.Optional;

public record Result(String message, CommandType commandType, State state, Optional<Context> context,
                     List<List<String>> inlineKeyboard) {

    public static Result stay(String message, CommandType type) {
        return new Result(message, type, null, Optional.empty(), List.of());
    }

    public static Result stay(String message, CommandType type, List<List<String>> inlineKeyboard) {
        return new Result(message, type, null, Optional.empty(), inlineKeyboard);
    }

    public static Result transition(String message, CommandType type, State newState, Context newContext) {
        return new Result(message, type, newState, Optional.ofNullable(newContext), List.of());
    }
}
