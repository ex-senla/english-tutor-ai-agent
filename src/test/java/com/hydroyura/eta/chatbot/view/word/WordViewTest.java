package com.hydroyura.eta.chatbot.view.word;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.view.Buttons;
import com.hydroyura.eta.chatbot.view.Callbacks;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordViewTest {

    @Test
    void shouldLabelAllPartsOfSpeech() {
        assertThat(WordView.posLabel(PartOfSpeech.NOUN)).isEqualTo(Buttons.NOUN);
        assertThat(WordView.posLabel(PartOfSpeech.VERB)).isEqualTo(Buttons.VERB);
        assertThat(WordView.posLabel(PartOfSpeech.ADJECTIVE)).isEqualTo(Buttons.ADJECTIVE);
        assertThat(WordView.posLabel(PartOfSpeech.EXPRESSION)).isEqualTo(Buttons.EXPRESSION);
    }

    @Test
    void shouldMapAllCallbackPayloads() {
        assertThat(WordView.fromCallback(Callbacks.NOUN)).isEqualTo(PartOfSpeech.NOUN);
        assertThat(WordView.fromCallback(Callbacks.VERB)).isEqualTo(PartOfSpeech.VERB);
        assertThat(WordView.fromCallback(Callbacks.ADJECTIVE)).isEqualTo(PartOfSpeech.ADJECTIVE);
        assertThat(WordView.fromCallback(Callbacks.EXPRESSION)).isEqualTo(PartOfSpeech.EXPRESSION);
        assertThatThrownBy(() -> WordView.fromCallback("adverb"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRenderFourPosButtons() {
        var result = WordView.posMenu("apple");

        var keyboard = ((ActionResult.TextWithInlineKeyboard) result).keyboard();
        assertThat(keyboard).hasSize(4);
        assertThat(keyboard.get(3).get(0).callbackData()).isEqualTo("pos:expression");
    }

    @Test
    void shouldRenderConfirmationWithTranslationsOwnAndChangePos() {
        var result = WordView.wordConfirmation("apple", PartOfSpeech.NOUN, List.of("яблоко", "фрукт"));

        var keyboard = ((ActionResult.TextWithInlineKeyboard) result).keyboard();
        assertThat(keyboard).hasSize(4);
        assertThat(keyboard.get(0).get(0).callbackData()).isEqualTo("translation:0");
        assertThat(keyboard.get(1).get(0).callbackData()).isEqualTo("translation:1");
        assertThat(keyboard.get(2).get(0).callbackData()).isEqualTo("translation:own");
        assertThat(keyboard.get(3).get(0).callbackData()).isEqualTo("poschange");
    }
}
