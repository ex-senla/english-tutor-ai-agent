package com.hydroyura.eta.chatbot.domain.statemachine;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.CommandExecutionResult;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public final class StateMachine {

    private final StateMachineId id;
    private final Context context = new Context();

    private State state;
    private Class<? extends Command> activeCommand;


    public static StateMachine ofDefaults(StateMachineId id) {
        return new StateMachine(id);
    }

    public Optional<Class<? extends Command>> getActiveCommandSafely() {
        return Optional.ofNullable(activeCommand);
    }

    public StateMachineExecutionResult executeCommand(Command command, String message) {
        validateCommand(command);
        var commandExecutionresult = command.execute(context, message);
        return convertResult(commandExecutionresult);
    }

    private void validateCommand(Command command) {
        if (Objects.nonNull(activeCommand) && !activeCommand.equals(command.getClass())) {
            throw new RuntimeException("Invalid command"); // TODO: replace with correct exception
        }
        // TODO: add other validations
    }

    private StateMachineExecutionResult convertResult(CommandExecutionResult commandExecutionResult) {
        return null;
    }


}