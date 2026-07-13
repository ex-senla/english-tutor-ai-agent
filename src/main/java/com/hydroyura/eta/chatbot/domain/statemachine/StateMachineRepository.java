package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.Optional;

public interface StateMachineRepository {

    Optional<StateMachine> findById(StateMachineId id);

    void save(StateMachine sm);

}
