package com.hydroyura.eta.chatbot.domain.statemachine;

import com.hydroyura.eta.chatbot.domain.command.Command;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
public final class StateMachine {

    private final StateMachineId id;
    private State state;
    @Setter
    private Class<? extends Command> pendingCommand;
    private Context context = new Context();

    private StateMachine(StateMachineId id) {
        this.id = id;
        this.state = State.NOT_REGISTER;
    }

    public static StateMachine ofDefaults(StateMachineId id) {
        return new StateMachine(id);
    }

    public String execute(Command command, String userMessage) {
        if (!state.allows(command.type())) {
            return "Command not available in state: " + state + ". /help";
        }
        var result = command.execute(this, userMessage);
        var newState = result.state();
        if (newState != null && newState != this.state) {
            this.state = newState;
        }
        result.context().ifPresent(ctx -> this.context = ctx);
        return result.message();
    }

    public Optional<Class<? extends Command>> getPendingCommandSafely() {
        return Optional.ofNullable(pendingCommand);
    }

    public void clearPendingCommand() {
        this.pendingCommand = null;
    }

}
