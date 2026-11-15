package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.word.WordTranslationCommand;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

@Slf4j
@RequiredArgsConstructor
public class PosCbAwaitingPosTransition implements Transition<Action.Callback> {

    private final WordTranslator wordTranslator;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var pos = WordView.fromCallback(callback.payload());
        chat.getContext().put(WORD_POS.getValue(), pos);

        var word = (String) chat.getContext().get(WORD_VALUE.getValue());
        var translations = wordTranslator.translate(new WordTranslationCommand(word, pos));

        if (translations.isEmpty()) {
            chat.updateState(ChatState.AWAITING_MANUAL_TRANSLATION);
            return WordView.manualTranslationPrompt(callback, word, pos);
        }

        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.copyOf(translations));
        chat.updateState(ChatState.AWAITING_TRANSLATION_CHOICE);
        return WordView.translationChoice(callback, word, pos, translations);
    }
}
