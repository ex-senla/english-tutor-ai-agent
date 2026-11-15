package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.exercise.ExerciseWordSelectionView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FilterCbAwaitingExerciseWordSelectionTransition implements Transition<Action.Callback> {

    private final ExerciseWordSelectionSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        support.setFilter(chat, ExerciseWordSelectionView.filterFromPayload(callback.payload()));
        return support.render(chat, callback.messageId());
    }
}
