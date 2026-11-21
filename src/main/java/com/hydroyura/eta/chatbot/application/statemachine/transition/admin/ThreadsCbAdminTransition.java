package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;

@RequiredArgsConstructor
public class ThreadsCbAdminTransition implements Transition<Action.Callback> {

    private final FindSupportThreads findSupportThreads;

    private final TeacherDirectory teacherDirectory;

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }
        var rows = findSupportThreads.findAll().stream()
                .map(thread -> new AdminView.ThreadRow(thread, teacherName(thread.teacherId())))
                .toList();
        chat.updateState(ChatState.ADMIN_THREADS);
        return AdminView.threads(rows);
    }

    private String teacherName(com.hydroyura.eta.teacher.api.teacher.TeacherId teacherId) {
        return teacherDirectory.findById(teacherId)
                .map(info -> info.name())
                .orElse("?");
    }
}
