package com.hydroyura.eta.chatbot.application.statemachine.transition;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.ACTING_TEACHER_ID;

/**
 * Универсальная команда «домой»: завершает имперсонацию (если активна), сбрасывает любое
 * не-начальное состояние в {@code ACTIVE} и показывает главное меню по реальной роли чата.
 */
public class StartCmdTransition implements Transition<Action.Command> {

    private final ActiveMenuSupport activeMenuSupport;

    public StartCmdTransition(ActiveMenuSupport activeMenuSupport) {
        this.activeMenuSupport = activeMenuSupport;
    }

    @Override
    public ActionResult transit(Chat chat, Action.Command command) {
        chat.getContext().remove(ACTING_TEACHER_ID.getValue());
        chat.updateState(ChatState.ACTIVE);
        return activeMenuSupport.render(chat);
    }
}
