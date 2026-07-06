package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.shared.api.SnapshotProvider;
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
// TODO: remove SnapshotProvider when switching to JPA/PostgreSQL
public class InMemoryStateMachineRepository implements StateMachineRepository, SnapshotProvider {

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

    // TODO: remove when switching to JPA/PostgreSQL
    public Map<StateMachineId, StateMachine> snapshot() {
        return Map.copyOf(store);
    }
}
