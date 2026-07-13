package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineId;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class StateMachineAppService {

    private final StateMachineRepository repository;

    public StateMachine getOrCreate(Long chatId) {
        var id = new StateMachineId(chatId);
        return repository.findById(id).orElseGet(() -> create(id));
    }

    public void save(StateMachine sm) {
        repository.save(sm);
    }

    private StateMachine create(StateMachineId id) {
        log.info("create new stateMachine for id = '{}'", id);
        return StateMachine.ofDefaults(id);
    }
}
