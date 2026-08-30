package com.hydroyura.eta.chatbot.application.statemachine.transition.active;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.menu.MenuItem;

public class DefaultCmdActiveTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        var teacherName = (String) chat.getContext().getOrDefault("teacherName", "");
        return MenuItem.activeMenu(teacherName);
    }

    @Override
    public String getName() {
        return "DefaultCmdActiveTransition";
    }

}
