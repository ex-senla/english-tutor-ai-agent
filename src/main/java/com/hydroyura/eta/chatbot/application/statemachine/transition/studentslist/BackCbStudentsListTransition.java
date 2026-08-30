package com.hydroyura.eta.chatbot.application.statemachine.transition.studentslist;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.menu.MenuItem;

public class BackCbStudentsListTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.updateState(ChatState.ACTIVE);
        var teacherName = (String) chat.getContext().getOrDefault("teacherName", "");
        return MenuItem.activeMenu(teacherName, callback.messageId());
    }

    @Override
    public String getName() {
        return "BackCbStudentsListTransition";
    }
}
