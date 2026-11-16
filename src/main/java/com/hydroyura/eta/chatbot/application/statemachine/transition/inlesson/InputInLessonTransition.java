package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWordCommand;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

/**
 * Ввод слова в {@code IN_LESSON}: валидация → one-shot POS+переводы → экран подтверждения
 * (сбой LLM → ручное меню POS из четырёх кнопок).
 */
@RequiredArgsConstructor
public class InputInLessonTransition implements Transition<Action.Input> {

    private final CheckWord checkWord;

    private final WordTranslator wordTranslator;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var wordValue = input.text() == null ? "" : input.text();

        if (!checkWord.isValid(new CheckWordCommand(wordValue))) {
            return WordView.notValidWord();
        }

        var normalized = normalize(wordValue);
        var suggestion = wordTranslator.suggestPosAndTranslations(normalized);

        if (suggestion.partOfSpeech() == null || suggestion.translations().isEmpty()) {
            chat.getContext().put(WORD_VALUE.getValue(), normalized);
            chat.updateState(ChatState.AWAITING_POS);
            return WordView.posMenu(normalized);
        }

        chat.getContext().put(WORD_VALUE.getValue(), normalized);
        chat.getContext().put(WORD_POS.getValue(), suggestion.partOfSpeech());
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.copyOf(suggestion.translations()));
        chat.updateState(ChatState.AWAITING_TRANSLATION_CHOICE);
        return WordView.wordConfirmation(normalized, suggestion.partOfSpeech(), suggestion.translations());
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}
