package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentname;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;

import static com.hydroyura.eta.chatbot.view.Messages.CHOOSE_LEVEL;

public class InputAwaitingStudentNameTransition implements Transition<Action.Input> {

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        chat.getContext().put("pendingStudentName", input.text());
        chat.updateState(ChatState.AWAITING_STUDENT_LEVEL);
        return new ActionResult.TextWithInlineKeyboard(CHOOSE_LEVEL, StudentView.levelKeyboard());
    }
}
