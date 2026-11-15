package com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;

public class StudentSearchTransition implements Transition<Action> {

    @Override
    public ActionResult transit(Chat chat, Action action) {
        chat.updateState(ChatState.STUDENT_SEARCH);
        return StudentView.searchPrompt();
    }
}
