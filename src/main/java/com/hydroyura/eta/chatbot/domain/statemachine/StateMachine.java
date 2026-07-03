package com.hydroyura.eta.chatbot.domain.statemachine;

import com.hydroyura.eta.chatbot.domain.command.Command;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

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
        applyResult(result);
        return result.message();
    }

    public BotExecuteResult executeFull(Command command, String userMessage) {
        if (!state.allows(command.type())) {
            return new BotExecuteResult("Command not available in state: " + state + ". /help", List.of());
        }
        var result = command.execute(this, userMessage);
        applyResult(result);
        return new BotExecuteResult(result.message(), result.inlineKeyboard());
    }

    private void applyResult(com.hydroyura.eta.chatbot.domain.command.Result result) {
        var newState = result.state();
        if (newState != null && newState != this.state) {
            this.state = newState;
        }
        result.context().ifPresent(ctx -> this.context = ctx);
    }

    public Optional<Class<? extends Command>> getPendingCommandSafely() {
        return Optional.ofNullable(pendingCommand);
    }

    public void clearPendingCommand() {
        this.pendingCommand = null;
    }

}
