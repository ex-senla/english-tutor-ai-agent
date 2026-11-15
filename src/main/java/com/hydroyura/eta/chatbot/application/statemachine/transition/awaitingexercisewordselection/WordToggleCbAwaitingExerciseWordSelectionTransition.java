package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WordToggleCbAwaitingExerciseWordSelectionTransition implements Transition<Action.Callback> {

    private final ExerciseWordSelectionSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        support.toggleWord(chat, callback.payload());
        return support.render(chat, callback.messageId());
    }
}
