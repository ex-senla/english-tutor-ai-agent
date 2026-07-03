package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.Set;

import static com.hydroyura.eta.chatbot.domain.statemachine.CommandType.*;

public enum State {
    NOT_REGISTER(START, REGISTER, HELP),
    ACTIVE(START, NEW_STUDENT, START_LESSON, EXERCISE, HELP),
    IN_LESSON(ADD_WORD, END_LESSON, EXERCISE, HELP);

    private final Set<CommandType> allowedCommands;

    State(CommandType... commands) {
        this.allowedCommands = Set.of(commands);
    }

    public boolean allows(CommandType type) {
        return allowedCommands.contains(type);
    }

    public String[] keyboardButtons() {
        return switch (this) {
            case NOT_REGISTER -> new String[]{"/start", "/register", "/help"};
            case ACTIVE -> new String[]{"/newstudent", "/startlesson", "/exercise", "/help"};
            case IN_LESSON -> new String[]{"/add", "/endlesson", "/exercise", "/help"};
        };
    }
}
