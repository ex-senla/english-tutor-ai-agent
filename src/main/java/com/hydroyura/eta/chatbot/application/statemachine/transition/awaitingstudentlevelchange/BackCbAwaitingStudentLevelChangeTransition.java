package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingstudentlevelchange;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackCbAwaitingStudentLevelChangeTransition implements Transition<Action.Callback> {

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.updateState(ChatState.STUDENT_DETAILS);
        return StudentSupport.studentDetails(studentQuery, chat, callback.messageId());
    }
}
