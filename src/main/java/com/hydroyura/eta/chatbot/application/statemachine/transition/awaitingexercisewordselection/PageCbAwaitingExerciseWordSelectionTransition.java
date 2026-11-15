package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingexercisewordselection;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.Callbacks;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PageCbAwaitingExerciseWordSelectionTransition implements Transition<Action.Callback> {

    private final ExerciseWordSelectionSupport support;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var delta = Callbacks.NEXT.equals(callback.payload()) ? 1 : -1;
        support.setPage(chat, ExerciseWordSelectionSupport.currentPage(chat) + delta);
        return support.render(chat, callback.messageId());
    }
}
