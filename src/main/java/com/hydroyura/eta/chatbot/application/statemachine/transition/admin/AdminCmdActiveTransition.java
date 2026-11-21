package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static com.hydroyura.eta.chatbot.view.Messages.ADMIN_ACCESS_DENIED;

@RequiredArgsConstructor
public class AdminCmdActiveTransition implements Transition<Action> {

    private final AdminGuard adminGuard;

    @Override
    public ActionResult transit(Chat chat, Action action) {
        if (!adminGuard.isAdmin(chat)) {
            return new ActionResult.TextResponse(ADMIN_ACCESS_DENIED);
        }

        chat.getContext().remove(ACTING_TEACHER_ID.getValue());
        chat.updateState(ChatState.ADMIN_MENU);
        return AdminView.menu();
    }
}
