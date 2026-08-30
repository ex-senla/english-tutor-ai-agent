package com.hydroyura.eta.chatbot.application.statemachine.transition.active;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;

public class NewStudentBtnActiveTransition implements Transition<Action.Button> {

    @Override
    public ActionResult transit(Chat chat, Action.Button button) {
        chat.updateState(ChatState.AWAITING_STUDENT_NAME);
        return new ActionResult.TextResponse("Введите имя нового ученика");
    }

    @Override
    public String getName() {
        return "NewStudentBtnActiveTransition";
    }
}
