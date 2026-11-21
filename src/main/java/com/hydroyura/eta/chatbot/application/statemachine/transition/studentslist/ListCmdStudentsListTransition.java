package com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListCmdStudentsListTransition implements Transition<Action.Command> {

    private final ActingTeacherResolver resolver;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        var studentIds = resolver.actingStudentIds(chat, chat.getId().chatId());
        var students = studentQuery.findStudentsByIds(studentIds);
        return StudentView.studentsListMenu(students);
    }

}
