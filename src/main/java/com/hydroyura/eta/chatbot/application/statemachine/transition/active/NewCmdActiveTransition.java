package com.hydroyura.eta.chatbot.application.statemachine.transition.active;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NewCmdActiveTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        chat.updateState(ChatState.AWAITING_STUDENT_NAME);
        return new ActionResult.TextResponse("Введите имя нового ученика");
    }

    @Override
    public String getName() {
        return "NewCmdActiveTransition";
    }

}
