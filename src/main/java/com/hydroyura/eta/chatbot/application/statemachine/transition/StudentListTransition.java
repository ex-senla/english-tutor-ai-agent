package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class StudentListTransition implements Transition<Action> {

    private final ActingTeacherResolver resolver;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action action) {
        chat.updateState(ChatState.STUDENTS_LIST);
        var studentIds = resolver.actingStudentIds(chat, chat.getId().chatId());
        List<StudentInfo> students = studentQuery.findStudentsByIds(studentIds);
        return StudentView.studentsListMenu(students);
    }

}
