package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslationchoice;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.word.LessonWordSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.chatbot.view.lesson.LessonView;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static com.hydroyura.eta.chatbot.view.Messages.INVALID_INPUT;
import static com.hydroyura.eta.chatbot.view.Messages.WORD_NOT_FOUND;

@RequiredArgsConstructor
public class TranslationCbAwaitingTranslationChoiceTransition implements Transition<Action.Callback> {

    private final LessonWordSupport lessonWordSupport;

    @Override
    public ActionResult transit(Chat chat, Action.Callback callback) {
        var word = (String) chat.getContext().get(WORD_VALUE.getValue());
        var pos = (PartOfSpeech) chat.getContext().get(WORD_POS.getValue());

        if (Callbacks.OWN.equals(callback.payload())) {
            chat.updateState(ChatState.AWAITING_MANUAL_TRANSLATION);
            return WordView.manualTranslationPrompt(callback, word, pos);
        }

        var index = parseIndex(callback.payload());
        var translations = (List<String>) chat.getContext().get(WORD_TRANSLATIONS.getValue());
        if (index == null || translations == null || index < 0 || index >= translations.size()) {
            chat.updateState(ChatState.IN_LESSON);
            return LessonView.lessonKeyboard(WORD_NOT_FOUND);
        }

        try {
            return lessonWordSupport.addWord(chat, word, Set.of(translations.get(index)), pos);
        } catch (WordValidationException e) {
            chat.updateState(ChatState.IN_LESSON);
            return LessonView.lessonKeyboard(INVALID_INPUT);
        }
    }

    private Integer parseIndex(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(payload);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
