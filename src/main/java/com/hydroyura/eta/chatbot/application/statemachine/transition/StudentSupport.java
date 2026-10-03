package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import java.util.Objects;
import java.util.UUID;

/**
 * Общие операции переходов, работающих с выбранным учеником.
 */
public final class StudentSupport {

    private StudentSupport() {
    }

    public static StudentId selectedStudentId(Chat chat) {
        var raw = Objects.requireNonNull(chat.getContext().get("selectedStudentId"),
                "selectedStudentId must be present in chat context");
        var id = (String) raw;
        if (id.isBlank()) {
            throw new IllegalStateException("selectedStudentId must not be blank");
        }
        return new StudentId(UUID.fromString(id));
    }

    public static ActionResult studentDetails(StudentQuery studentQuery, Chat chat, int messageId) {
        var studentId = selectedStudentId(chat);
        var details = studentQuery.findStudentDetails(studentId)
                .orElseThrow(() -> new IllegalStateException("No details for student " + studentId));
        return StudentView.studentDetails(messageId, details);
    }
}
