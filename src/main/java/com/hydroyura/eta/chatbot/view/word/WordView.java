package com.hydroyura.eta.chatbot.view.word;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;

import java.util.List;

import static com.hydroyura.eta.chatbot.view.util.ItemUtils.createCallbackData;

public class WordView {

    public static ActionResult posMenu(String word) {
        var keyboard = List.of(
                List.of(new ActionResult.InlineButton(Buttons.NOUN, createCallbackData(Callbacks.POS, Callbacks.NOUN))),
                List.of(new ActionResult.InlineButton(Buttons.VERB, createCallbackData(Callbacks.POS, Callbacks.VERB))),
                List.of(new ActionResult.InlineButton(Buttons.ADJECTIVE, createCallbackData(Callbacks.POS,
                        Callbacks.ADJECTIVE)))
        );
        return new ActionResult.TextWithInlineKeyboard("""
                Слово: %s

                Выберите часть речи:""".formatted(word), keyboard);
    }

    public static String posLabel(PartOfSpeech pos) {
        return switch (pos) {
            case PartOfSpeech.NOUN -> Buttons.NOUN;
            case PartOfSpeech.VERB -> Buttons.VERB;
            case PartOfSpeech.ADJECTIVE -> Buttons.ADJECTIVE;
        };
    }

    public static PartOfSpeech fromCallback(String payload) {
        return switch (payload) {
            case Callbacks.NOUN -> PartOfSpeech.NOUN;
            case Callbacks.VERB -> PartOfSpeech.VERB;
            case Callbacks.ADJECTIVE -> PartOfSpeech.ADJECTIVE;
            default -> throw new IllegalArgumentException(
                    "Unknown part-of-speech callback payload: " + payload);
        };
    }

    public static ActionResult enterTranslation(Action.Callback callback, String word, PartOfSpeech pos) {
        return new ActionResult.EditMessageText(callback.messageId(),
                """
                        Слово: %s
                        Часть речи: %s

                        Введите переводы через запятую (например: дом, здание, строение)""".formatted(word, posLabel(
                        pos)), List.of());
    }

}
