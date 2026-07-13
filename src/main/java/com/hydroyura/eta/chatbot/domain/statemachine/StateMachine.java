package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class StateMachine {

    @Getter
    private final StateMachineId id;
    @Getter
    private State state;
    @Getter
    private final Map<String, Object> context = new HashMap<>();

    public static StateMachine ofDefaults(StateMachineId id) {
        var sm = new StateMachine(id);
        sm.updateState(State.INITIAL);
        return sm;
    }

    public void updateState(State state) {
        this.state = state;
    }
}
