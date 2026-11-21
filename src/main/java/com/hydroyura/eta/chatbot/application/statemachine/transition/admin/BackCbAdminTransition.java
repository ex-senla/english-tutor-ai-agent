package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActiveMenuSupport;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_ID;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ADMIN_SELECTED_TEACHER_NAME;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.SUPPORT_THREAD_ID;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;
import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

@RequiredArgsConstructor
public class BackCbAdminTransition implements Transition<Action.Callback> {

    private static final String BACK_PREFIX = Callbacks.BACK + ":";

    private final ActiveMenuSupport activeMenuSupport;

    private final TeacherDirectory teacherDirectory;

    private final FindSupportThreads findSupportThreads;

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }
        if (!callback.payload().startsWith(BACK_PREFIX)) {
            return new ActionResult.TextResponse(USE_BUTTONS_BELOW);
        }
        var target = callback.payload().substring(BACK_PREFIX.length());
        return switch (target) {
            case Callbacks.MAIN -> toActive(chat);
            case Callbacks.MENU -> toMenu(chat);
            case Callbacks.TEACHERS -> toTeachers(chat);
            case Callbacks.THREADS -> toThreads(chat);
            default -> new ActionResult.TextResponse(USE_BUTTONS_BELOW);
        };
    }

    private ActionResult toActive(Chat chat) {
        clearAdminSelection(chat);
        chat.getContext().remove(SUPPORT_THREAD_ID.getValue());
        chat.updateState(ChatState.ACTIVE);
        return activeMenuSupport.render(chat);
    }

    private ActionResult toMenu(Chat chat) {
        clearAdminSelection(chat);
        chat.getContext().remove(SUPPORT_THREAD_ID.getValue());
        chat.updateState(ChatState.ADMIN_MENU);
        return AdminView.menu();
    }

    private ActionResult toTeachers(Chat chat) {
        chat.getContext().remove(ADMIN_SELECTED_TEACHER_ID.getValue());
        chat.getContext().remove(ADMIN_SELECTED_TEACHER_NAME.getValue());
        chat.updateState(ChatState.ADMIN_TEACHERS);
        return AdminView.teachers(teacherDirectory.findAll());
    }

    private ActionResult toThreads(Chat chat) {
        chat.getContext().remove(SUPPORT_THREAD_ID.getValue());
        var rows = findSupportThreads.findAll().stream()
                .map(thread -> new AdminView.ThreadRow(thread, teacherDirectory.findById(thread.teacherId())
                        .map(info -> info.name())
                        .orElse("?")))
                .toList();
        chat.updateState(ChatState.ADMIN_THREADS);
        return AdminView.threads(rows);
    }

    private void clearAdminSelection(Chat chat) {
        chat.getContext().remove(ADMIN_SELECTED_TEACHER_ID.getValue());
        chat.getContext().remove(ADMIN_SELECTED_TEACHER_NAME.getValue());
    }
}
