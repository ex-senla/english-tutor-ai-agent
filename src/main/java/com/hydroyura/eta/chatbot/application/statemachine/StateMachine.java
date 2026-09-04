package com.hydroyura.eta.chatbot.application.statemachine;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.handler.Handler;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.TransitionKey;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class StateMachine {

    private final Map<TransitionKey, Transition<? super Action.Command>> commandTransitions = new HashMap<>();

    private final Map<ChatState, Transition<? super Action.Input>> inputTransitions = new EnumMap<>(ChatState.class);

    private final Map<TransitionKey, Transition<? super Action.Callback>> callbackTransitions = new HashMap<>();

    private final Map<TransitionKey, Transition<? super Action.Button>> buttonTransitions = new HashMap<>();

    private final Map<ChatState, Handler> defaultHandlers;

    public void onCommand(ChatState initialState, String command, Transition<? super Action.Command> transition) {
        commandTransitions.put(new TransitionKey(initialState, command), transition);
    }

    public void onInput(ChatState initialState, Transition<? super Action.Input> transition) {
        inputTransitions.put(initialState, transition);
    }

    public void onCallback(ChatState initialState, String command, Transition<? super Action.Callback> transition) {
        callbackTransitions.put(new TransitionKey(initialState, command), transition);
    }

    public void onButton(ChatState initialState, String command, Transition<? super Action.Button> transition) {
        buttonTransitions.put(new TransitionKey(initialState, command), transition);
    }

    public ActionResult applyAction(Chat chat, Action action) {
        return switch (action) {
            case Action.Command command ->
                    apply(chat, commandTransitions.get(new TransitionKey(chat.getState(), command.command())), command);
            case Action.Input input -> apply(chat, inputTransitions.get(chat.getState()), input);
            case Action.Callback callback ->
                    apply(chat, callbackTransitions.get(new TransitionKey(chat.getState(), callback.prefix())), callback);
            case Action.Button button ->
                    apply(chat, buttonTransitions.get(new TransitionKey(chat.getState(), button.command())), button);
        };
    }

    private <T extends Action> ActionResult apply(Chat chat, Transition<? super T> transition, T action) {
        if (transition == null) {
            return defaultHandlers.get(chat.getState()).handle(chat);
        }

        return transition.transit(chat, action);
    }

    //TODO переделать проверку
    public boolean isReady() {
        return !inputTransitions.containsValue(null);
    }

}
