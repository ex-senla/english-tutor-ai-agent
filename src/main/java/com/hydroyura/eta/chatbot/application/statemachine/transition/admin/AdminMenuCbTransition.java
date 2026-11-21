package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.Callbacks;

import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

public class AdminMenuCbTransition implements Transition<Action.Callback> {

    private final TeachersCbAdminTransition teachers;

    private final ThreadsCbAdminTransition threads;

    private final BackCbAdminTransition back;

    public AdminMenuCbTransition(TeachersCbAdminTransition teachers, ThreadsCbAdminTransition threads,
            BackCbAdminTransition back) {
        this.teachers = teachers;
        this.threads = threads;
        this.back = back;
    }

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        return switch (callback.payload()) {
            case Callbacks.TEACHERS -> teachers.transit(chat, callback);
            case Callbacks.THREADS -> threads.transit(chat, callback);
            case Callbacks.BACK + ":" + Callbacks.MAIN -> back.transit(chat, callback);
            default -> new ActionResult.TextResponse(USE_BUTTONS_BELOW);
        };
    }
}
