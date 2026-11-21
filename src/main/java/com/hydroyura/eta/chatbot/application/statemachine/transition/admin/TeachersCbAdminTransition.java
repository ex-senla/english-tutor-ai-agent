package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;

@RequiredArgsConstructor
public class TeachersCbAdminTransition implements Transition<Action.Callback> {

    private final TeacherDirectory teacherDirectory;

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }
        chat.updateState(ChatState.ADMIN_TEACHERS);
        return AdminView.teachers(teacherDirectory.findAll());
    }
}
