package com.hydroyura.eta.chatbot.item.word;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;

import java.util.List;

public class WordItem {

    public static ActionResult posMenu(String word) {
        var keyboard = List.of(
                List.of(new ActionResult.InlineButton("📛 Noun", "pos:NOUN")),
                List.of(new ActionResult.InlineButton("🏃 Verb", "pos:VERB")),
                List.of(new ActionResult.InlineButton("🎨 Adjective", "pos:ADJECTIVE"))
        );
        return new ActionResult.TextWithInlineKeyboard("Слово: " + word + "\n\nВыберите часть речи:", keyboard);
    }

    public static String posLabel(PartOfSpeech pos) {
        return switch (pos) {
            case NOUN -> "📛 Noun";
            case VERB -> "🏃 Verb";
            case ADJECTIVE -> "🎨 Adjective";
        };
    }

}
