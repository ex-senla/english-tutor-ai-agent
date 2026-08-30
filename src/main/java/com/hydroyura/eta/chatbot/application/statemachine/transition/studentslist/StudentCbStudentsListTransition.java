package com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.students.StudentItem;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudentCbStudentsListTransition implements Transition<Action.Callback> {

    private final FindTeacher findTeacher;

    private final StudentQuery studentQuery;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var studentId = callback.payload();
        chat.getContext().put("selectedStudentId", studentId);
        chat.updateState(ChatState.STUDENT_OPTIONS);

        var studentIds = findTeacher.getStudentIds(chat.getId().chatId());
        var name = studentQuery.findStudentsByIds(studentIds).stream()
                .filter(s -> s.id().value().toString().equals(studentId))
                .map(StudentInfo::name)
                .findFirst()
                .orElse("?");

        chat.getContext().put("selectedStudentName", name);
        return StudentItem.options(callback.messageId(), name);
    }

    @Override
    public String getName() {
        return "StudentCbStudentsListTransition";
    }
}
