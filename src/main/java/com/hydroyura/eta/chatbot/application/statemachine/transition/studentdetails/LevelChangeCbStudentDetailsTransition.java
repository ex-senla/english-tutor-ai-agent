package com.hydroyura.eta.chatbot.application.statemachine.transition.studentdetails;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;

import static com.hydroyura.eta.chatbot.view.Messages.CHOOSE_NEW_LEVEL;

public class LevelChangeCbStudentDetailsTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.updateState(ChatState.AWAITING_STUDENT_LEVEL_CHANGE);
        return new ActionResult.EditMessageText(callback.messageId(), CHOOSE_NEW_LEVEL,
                StudentView.levelKeyboardWithBack());
    }
}
