package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;

/**
 * Ожидает и кнопку «➕ Добавить слово», и команду /addword —
 * логика не зависит от типа триггера, поэтому типизирован общим {@link Action}.
 */
public class AddWordInLessonTransition implements Transition<Action> {

    @Override
    public ActionResult transit(Chat chat, Action action) {
        chat.updateState(ChatState.AWAITING_WORD);
        return new ActionResult.TextResponse("Введите слово на английском");
    }

    @Override
    public String getName() {
        return "AddWordInLessonTransition";
    }
}
