package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.CommandDispatcher;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineExecutionResult;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineId;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StateMachineAppService {

    private final StateMachineRepository repository;
    private final CommandDispatcher dispatcher;

    public StateMachineExecutionResult handle(Long chatId, String message) {
        // 1. Get sm
        var sm = getStateMachine(chatId);

        // 2. Get command
        var command = getCommand(sm, message);

        // 3. execute command
        var result = sm.executeCommand(command, message);

        // 4. save stateMachine
        repository.save(sm);
        return result;
    }

    private StateMachine getStateMachine(Long chatId) {
        var id = new StateMachineId(chatId);
        return repository.findById(id).orElse(StateMachine.ofDefaults(id));
    }

    private Command getCommand(StateMachine sm, String message) {
        return sm.getActiveCommandSafely()
                .map(dispatcher::get)
                .orElseGet(() -> dispatcher.dispatch(message));
    }


}
