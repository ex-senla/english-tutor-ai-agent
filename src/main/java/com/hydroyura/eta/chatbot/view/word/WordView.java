package com.hydroyura.eta.chatbot.view.word;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordStatus;

import java.util.ArrayList;
import java.util.List;

import static com.hydroyura.eta.chatbot.view.Messages.INVALID_INPUT;
import static com.hydroyura.eta.chatbot.view.Messages.TRANSLATION_PROMPT;
import static com.hydroyura.eta.chatbot.view.Messages.TRANSLATION_SUGGESTIONS_FAILED;
import static com.hydroyura.eta.chatbot.view.util.ViewUtils.createCallbackData;

public class WordView {

    public static ActionResult posMenu(String word) {
        return new ActionResult.TextWithInlineKeyboard(posMenuText(word), posMenuKeyboard());
    }

    public static ActionResult posMenuEdit(int messageId, String word) {
        return new ActionResult.EditMessageText(messageId, posMenuText(word), posMenuKeyboard());
    }

    private static String posMenuText(String word) {
        return """
                Слово: %s

                Выберите часть речи:""".formatted(word);
    }

    private static List<List<ActionResult.InlineButton>> posMenuKeyboard() {
        return List.of(
                List.of(new ActionResult.InlineButton(Buttons.NOUN, createCallbackData(Callbacks.POS, Callbacks.NOUN))),
                List.of(new ActionResult.InlineButton(Buttons.VERB, createCallbackData(Callbacks.POS, Callbacks.VERB))),
                List.of(new ActionResult.InlineButton(Buttons.ADJECTIVE, createCallbackData(Callbacks.POS,
                        Callbacks.ADJECTIVE))),
                List.of(new ActionResult.InlineButton(Buttons.EXPRESSION, createCallbackData(Callbacks.POS,
                        Callbacks.EXPRESSION)))
        );
    }

    public static String posLabel(PartOfSpeech pos) {
        return switch (pos) {
            case PartOfSpeech.NOUN -> Buttons.NOUN;
            case PartOfSpeech.VERB -> Buttons.VERB;
            case PartOfSpeech.ADJECTIVE -> Buttons.ADJECTIVE;
            case PartOfSpeech.EXPRESSION -> Buttons.EXPRESSION;
        };
    }

    public static String statusLabel(WordStatus status) {
        return switch (status) {
            case WordStatus.NEW -> "🆕";
            case WordStatus.IN_PROGRESS -> "🔄";
            case WordStatus.LEARNED -> "✅";
        };
    }

    public static PartOfSpeech fromCallback(String payload) {
        return switch (payload) {
            case Callbacks.NOUN -> PartOfSpeech.NOUN;
            case Callbacks.VERB -> PartOfSpeech.VERB;
            case Callbacks.ADJECTIVE -> PartOfSpeech.ADJECTIVE;
            case Callbacks.EXPRESSION -> PartOfSpeech.EXPRESSION;
            default -> throw new IllegalArgumentException(
                    "Unknown part-of-speech callback payload: " + payload);
        };
    }

    public static ActionResult wordConfirmation(String word, PartOfSpeech pos, List<String> translations) {
        return new ActionResult.TextWithInlineKeyboard(confirmationText(word, pos), translationKeyboard(translations));
    }

    public static ActionResult translationChoice(Action.Callback callback, String word, PartOfSpeech pos,
            List<String> translations) {
        return new ActionResult.EditMessageText(callback.messageId(), confirmationText(word, pos),
                translationKeyboard(translations));
    }

    private static String confirmationText(String word, PartOfSpeech pos) {
        return """
                Слово: %s
                Часть речи: %s

                Выберите перевод:""".formatted(word, posLabel(pos));
    }

    private static List<List<ActionResult.InlineButton>> translationKeyboard(List<String> translations) {
        var keyboard = new ArrayList<List<ActionResult.InlineButton>>();
        for (var i = 0; i < translations.size(); i++) {
            var index = i;
            keyboard.add(List.of(new ActionResult.InlineButton(translations.get(index),
                    createCallbackData(Callbacks.TRANSLATION, String.valueOf(index)))));
        }
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.OWN_TRANSLATION,
                createCallbackData(Callbacks.TRANSLATION, Callbacks.OWN))));
        keyboard.add(List.of(new ActionResult.InlineButton(Buttons.CHANGE_POS,
                createCallbackData(Callbacks.POS_CHANGE))));
        return keyboard;
    }

    public static ActionResult manualTranslationPrompt(Action.Callback callback, String word, PartOfSpeech pos) {
        return new ActionResult.EditMessageText(callback.messageId(),
                """
                        Слово: %s
                        Часть речи: %s

                        %s
                        %s""".formatted(word, posLabel(pos), TRANSLATION_SUGGESTIONS_FAILED, TRANSLATION_PROMPT),
                List.of());
    }

    public static ActionResult notValidWord() {
        return new ActionResult.TextResponse(
                """
                        %s.

                        Введите слово на английском""".formatted(INVALID_INPUT)
        );
    }

    public static ActionResult notValidTranslations() {
        return new ActionResult.TextResponse(
                """
                        %s.

                        Введите переводы на русском""".formatted(INVALID_INPUT)
        );
    }

}
