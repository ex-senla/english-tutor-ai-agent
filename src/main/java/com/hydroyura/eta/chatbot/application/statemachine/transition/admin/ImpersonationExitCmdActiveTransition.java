package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.ActingTeacherResolver;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.admin.AdminView;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;
import static com.hydroyura.eta.chatbot.view.Messages.NOTHING_TO_CANCEL;

@RequiredArgsConstructor
public class ImpersonationExitCmdActiveTransition implements Transition<Action.Command> {

    private final ActingTeacherResolver resolver;

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        if (!resolver.isImpersonating(chat)) {
            return new ActionResult.TextResponse(NOTHING_TO_CANCEL);
        }

        chat.getContext().remove(ACTING_TEACHER_ID.getValue());
        chat.updateState(ChatState.ADMIN_MENU);
        return AdminView.menu();
    }
}
