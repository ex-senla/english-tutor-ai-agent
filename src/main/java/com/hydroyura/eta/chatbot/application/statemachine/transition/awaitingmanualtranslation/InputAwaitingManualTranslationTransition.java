package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingmanualtranslation;

import com.hydroyura.eta.chatbot.application.statemachine.transition.Transition;
import com.hydroyura.eta.chatbot.application.statemachine.transition.word.LessonWordSupport;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.view.word.WordView;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;

@RequiredArgsConstructor
public class InputAwaitingManualTranslationTransition implements Transition<Action.Input> {

    private final LessonWordSupport lessonWordSupport;

    @Override
    public ActionResult transit(Chat chat, Action.Input input) {
        var word = (String) chat.getContext().get(WORD_VALUE.getValue());
        var pos = (PartOfSpeech) chat.getContext().get(WORD_POS.getValue());

        var translations = Arrays.stream(input.text().split("\\s*,\\s*"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (translations.isEmpty()) {
            return WordView.notValidTranslations();
        }

        try {
            return lessonWordSupport.addWord(chat, word, translations, pos);
        } catch (WordValidationException e) {
            return WordView.notValidTranslations();
        }
    }
}
