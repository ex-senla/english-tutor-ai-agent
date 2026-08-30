package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.item.word.WordItem;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;

import java.util.List;

public class PosCbAwaitingPosTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var pos = PartOfSpeech.valueOf(callback.payload());
        chat.getContext().put("wordPos", pos);
        chat.updateState(ChatState.AWAITING_TRANSLATION);
        var word = (String) chat.getContext().get("wordValue");
        return new ActionResult.EditMessageText(callback.messageId(),
                "Слово: " + word + "\nЧасть речи: " + WordItem.posLabel(pos)
                        + "\n\nВведите переводы через запятую (например: дом, здание, строение)", List.of());
    }

    @Override
    public String getName() {
        return "PosCbAwaitingPosTransition";
    }
}
