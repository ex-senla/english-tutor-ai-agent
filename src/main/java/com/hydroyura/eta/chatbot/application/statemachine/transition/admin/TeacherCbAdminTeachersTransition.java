package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_NAME;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;

@RequiredArgsConstructor
public class TeacherCbAdminTeachersTransition implements Transition<Action.Callback> {

    private final TeacherDirectory teacherDirectory;

    private final StudentQuery studentQuery;

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }
        var teacherId = new TeacherId(UUID.fromString(callback.payload()
                .substring((com.hydroyura.eta.chatbot.view.Callbacks.TEACHER + ":").length())));
        var teacher = teacherDirectory.findById(teacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher not found: " + teacherId));
        var students = studentQuery.findStudentsByIds(teacherDirectory.getStudentIds(teacherId));

        chat.getContext().put(ADMIN_SELECTED_TEACHER_ID.getValue(), teacherId.value().toString());
        chat.getContext().put(ADMIN_SELECTED_TEACHER_NAME.getValue(), teacher.name());
        chat.updateState(ChatState.ADMIN_TEACHER_STUDENTS);
        return AdminView.teacher(teacher.name(), students, teacherId.value().toString());
    }
}
