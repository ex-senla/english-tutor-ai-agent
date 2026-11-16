package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslationchoice;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.word.WordView;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

/**
 * «🔁 Изменить часть речи» — возвращает к ручному меню POS из четырёх кнопок.
 */
public class PosChangeCbAwaitingTranslationChoiceTransition implements Transition<Action.Callback> {

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        chat.getContext().remove(WORD_POS.getValue());
        chat.getContext().remove(WORD_TRANSLATIONS.getValue());
        chat.updateState(ChatState.AWAITING_POS);

        var word = (String) chat.getContext().get(WORD_VALUE.getValue());
        return WordView.posMenuEdit(callback.messageId(), word);
    }
}
