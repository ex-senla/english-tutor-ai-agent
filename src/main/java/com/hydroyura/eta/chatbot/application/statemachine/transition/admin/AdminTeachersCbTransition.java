package com.hydroyura.eta.chatbot.application.statemachine.transition.admin;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.Callbacks;

import static com.hydroyura.eta.chatbot.view.Messages.USE_BUTTONS_BELOW;

public class AdminTeachersCbTransition implements Transition<Action.Callback> {

    private final TeacherCbAdminTeachersTransition teacher;

    private final BackCbAdminTransition back;

    public AdminTeachersCbTransition(TeacherCbAdminTeachersTransition teacher, BackCbAdminTransition back) {
        this.teacher = teacher;
        this.back = back;
    }

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        if (callback.payload().startsWith(Callbacks.TEACHER + ":")) {
            return teacher.transit(chat, callback);
        }
        if (callback.payload().equals(Callbacks.BACK + ":" + Callbacks.MENU)) {
            return back.transit(chat, callback);
        }
        return new ActionResult.TextResponse(USE_BUTTONS_BELOW);
    }
}
