package com.hydroyura.eta.chatbot.domain.statemachine;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
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

    public ActionResult performAction(Action action) {
        switch (action) {
            case Action.Command c -> System.out.println(c);
            case Action.InputParam ip -> System.out.println(ip);
            case Action.Callback cb -> System.out.println(cb);
        }

        return null;
    }

}
