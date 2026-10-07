package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisegrammar;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.GRAMMAR_RULE;
import static com.hydroyura.eta.chatbot.view.Messages.CHOOSE_EXERCISE_TOPIC;

public class InputAwaitingExerciseGrammarTransition implements Transition<Action.Input> {

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        chat.getContext().put(GRAMMAR_RULE.getValue(), input.text());
        chat.updateState(ChatState.AWAITING_EXERCISE_TOPIC);
        return new ActionResult.TextResponse(CHOOSE_EXERCISE_TOPIC);
    }
}
