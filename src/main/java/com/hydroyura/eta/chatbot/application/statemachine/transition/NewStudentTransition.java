package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;

import static com.hydroyura.eta.chatbot.view.Messages.ENTER_NEW_STUDENT_NAME;

public class NewStudentTransition implements Transition<Action> {

    @Override
    public ActionResult transit(Chat chat, Action action) {
        chat.updateState(ChatState.AWAITING_STUDENT_NAME);
        return new ActionResult.TextResponse(ENTER_NEW_STUDENT_NAME);
    }

}
