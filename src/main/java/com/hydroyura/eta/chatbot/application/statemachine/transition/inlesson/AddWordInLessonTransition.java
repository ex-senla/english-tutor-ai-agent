package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_WORD_IN_ENGLISH;

public class AddWordInLessonTransition implements Transition<Action> {

    @Override
    public ActionResult transit(Chat chat, Action action) {
        chat.updateState(ChatState.AWAITING_WORD);
        return new ActionResult.TextResponse(ENTER_WORD_IN_ENGLISH);
    }

}
