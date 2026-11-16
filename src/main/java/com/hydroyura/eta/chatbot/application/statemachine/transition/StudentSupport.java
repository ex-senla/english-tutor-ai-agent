package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.students.StudentView;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.Objects;
import java.util.UUID;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SELECTED_STUDENT_NAME;

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

    public static ActionResult studentDetailsFresh(StudentQuery studentQuery, Chat chat) {
        var studentId = selectedStudentId(chat);
        var details = studentQuery.findStudentDetails(studentId)
                .orElseThrow(() -> new IllegalStateException("No details for student " + studentId));
        return StudentView.studentDetailsFresh(details);
    }

    /**
     * Обрабатывает выбор ученика из списка/поиска: если ученик не найден (удалён или не существует),
     * показывает «Ученик не найден или удалён» и актуальный список, не засоряя контекст.
     */
    public static ActionResult selectStudentOrNotFound(FindTeacher findTeacher, StudentQuery studentQuery, Chat chat,
            String studentId, int messageId) {
        var studentIds = findTeacher.getStudentIds(chat.getId().chatId());
        var students = studentQuery.findStudentsByIds(studentIds);

        var selected = students.stream()
                .filter(s -> s.id().value().toString().equals(studentId))
                .findFirst();

        if (selected.isEmpty()) {
            chat.updateState(ChatState.STUDENTS_LIST);
            return StudentView.studentNotFoundOrDeleted(messageId, students);
        }

        chat.getContext().put(SELECTED_STUDENT_ID.getValue(), studentId);
        chat.getContext().put(SELECTED_STUDENT_NAME.getValue(), selected.get().name());
        chat.updateState(ChatState.STUDENT_OPTIONS);
        return StudentView.options(messageId, selected.get().name());
    }
}
