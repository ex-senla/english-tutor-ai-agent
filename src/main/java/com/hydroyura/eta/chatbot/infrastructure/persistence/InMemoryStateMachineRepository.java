package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineId;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachineRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Repository
public class InMemoryStateMachineRepository implements StateMachineRepository {

    private final Map<StateMachineId, StateMachine> store = new ConcurrentHashMap<>();

    @Override
    public Optional<StateMachine> findById(StateMachineId id) {
        var sm = store.get(id);
        if (sm == null) {
            sm = StateMachine.ofDefaults(id);
            store.put(id, sm);
        }
        return Optional.of(sm);
    }

    @Override
    public void save(StateMachine stateMachine) {
        store.put(stateMachine.getId(), stateMachine);
    }
}
