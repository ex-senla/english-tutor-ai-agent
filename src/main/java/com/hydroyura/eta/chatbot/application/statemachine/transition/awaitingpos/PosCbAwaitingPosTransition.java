package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.word.WordView;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

public class PosCbAwaitingPosTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var pos = WordView.fromCallback(callback.payload());
        chat.getContext().put(WORD_POS.getValue(), pos);
        chat.updateState(ChatState.AWAITING_TRANSLATION);
        var word = (String) chat.getContext().get(WORD_VALUE.getValue());
        return WordView.enterTranslation(callback, word, pos);
    }

}
