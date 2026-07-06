package com.hydroyura.eta.chatbot.domain.statemachine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class StateMachine {

    private final StateMachineId id;
    private State state;

    public static StateMachine ofDefaults(StateMachineId id) {
        var sm = new StateMachine(id);
        sm.updateState(State.INITIAL);
        return sm;
    }

    public void updateState(State state) {
        this.state = state;
    }
}
