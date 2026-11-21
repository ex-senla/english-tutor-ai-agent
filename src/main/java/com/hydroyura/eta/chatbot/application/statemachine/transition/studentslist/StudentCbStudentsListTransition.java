package com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.StudentSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudentCbStudentsListTransition implements Transition<Action.Callback> {

    private final ActingTeacherResolver resolver;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return StudentSupport.selectStudentOrNotFound(resolver, studentQuery, chat, callback.payload(),
                callback.messageId());
    }

}
