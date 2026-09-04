package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingword;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.view.word.WordView;

public class InputAwaitingWordTransition implements Transition<Action.Input> {

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        chat.getContext().put("wordValue", input.text());
        chat.updateState(ChatState.AWAITING_POS);
        return WordView.posMenu(input.text());
    }

}
