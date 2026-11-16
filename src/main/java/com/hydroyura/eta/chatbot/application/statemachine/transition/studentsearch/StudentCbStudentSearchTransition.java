package com.hydroyura.eta.chatbot.application.statemachine.transition.studentsearch;

import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudentCbStudentSearchTransition implements Transition<Action.Callback> {

    private final FindTeacher findTeacher;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return StudentSupport.selectStudentOrNotFound(findTeacher, studentQuery, chat, callback.payload(),
                callback.messageId());
    }
}
