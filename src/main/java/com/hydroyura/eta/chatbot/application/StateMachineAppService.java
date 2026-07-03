package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.command.CommandDispatcher;
import com.hydroyura.eta.chatbot.domain.statemachine.State;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineId;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StateMachineAppService {

    private final StateMachineRepository repository;
    private final CommandDispatcher dispatcher;

    public BotResponse handle(String message, Long chatId) {
        var id = new StateMachineId(chatId);
        var sm = repository.findById(id).orElse(StateMachine.ofDefaults(id));

        var command = sm.getPendingCommandSafely()
            .map(dispatcher::get)
            .orElseGet(() -> dispatcher.dispatch(message));

        if (command == null) {
            return new BotResponse("Unknown command. /help");
        }

        var result = sm.executeFull(command, message);
        repository.save(sm);
        return new BotResponse(result.message(), result.inlineKeyboard());
    }

    public State getState(Long chatId) {
        return repository.findById(new StateMachineId(chatId))
            .map(StateMachine::getState)
            .orElse(State.NOT_REGISTER);
    }
}
