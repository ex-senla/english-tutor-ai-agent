package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.menu.MenuView;

/**
 * Универсальная команда «домой»: сбрасывает любое не-начальное состояние в {@code ACTIVE}
 * и показывает главное меню (reply-клавиатура).
 */
public class StartCmdTransition implements Transition<Action.Command> {

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        chat.updateState(ChatState.ACTIVE);
        return MenuView.activeMenu();
    }
}
