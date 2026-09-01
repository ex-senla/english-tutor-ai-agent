package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.word.WordView;

public class PosCbAwaitingPosTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var pos = WordView.fromCallback(callback.payload());
        chat.getContext().put("wordPos", pos);
        chat.updateState(ChatState.AWAITING_TRANSLATION);
        var word = (String) chat.getContext().get("wordValue");
        return WordView.enterTranslation(callback, word, pos);
    }

}
