package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingpos;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordTranslationCommand;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PosCbAwaitingPosTransitionTest {

    @Test
    void shouldShowTranslationVariants() {
        var wordTranslator = mock(WordTranslator.class);
        when(wordTranslator.translate(any())).thenReturn(List.of("яблоко", "фрукт", "яблочко"));
        var transition = new PosCbAwaitingPosTransition(wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_POS);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");

        var result = transition.transit(chat, new Action.Callback("pos", "noun", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_TRANSLATION_CHOICE);
        assertThat(chat.getContext().get(WORD_POS.getValue())).isEqualTo(PartOfSpeech.NOUN);
        assertThat((List<?>) chat.getContext().get(WORD_TRANSLATIONS.getValue())).hasSize(3);

        var edit = (ActionResult.EditMessageText) result;
        assertThat(edit.keyboard()).hasSize(5);
        assertThat(edit.keyboard().get(3).get(0).callbackData()).isEqualTo("translation:own");
    }

    @Test
    void shouldMoveToManualTranslationOnEmptyResult() {
        var wordTranslator = mock(WordTranslator.class);
        when(wordTranslator.translate(any())).thenReturn(List.of());
        var transition = new PosCbAwaitingPosTransition(wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_POS);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");

        var result = transition.transit(chat, new Action.Callback("pos", "verb", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_MANUAL_TRANSLATION);
        assertThat(chat.getContext().get(WORD_POS.getValue())).isEqualTo(PartOfSpeech.VERB);
        assertThat(chat.getContext()).doesNotContainKey(WORD_TRANSLATIONS.getValue());
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }

    @Test
    void shouldPassWordAndPosToTranslator() {
        var wordTranslator = mock(WordTranslator.class);
        when(wordTranslator.translate(any())).thenReturn(List.of("яблоко"));
        var transition = new PosCbAwaitingPosTransition(wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.getContext().put(WORD_VALUE.getValue(), "apple");

        transition.transit(chat, new Action.Callback("pos", "adjective", 42));

        var captor = ArgumentCaptor.forClass(WordTranslationCommand.class);
        verify(wordTranslator).translate(captor.capture());
        assertThat(captor.getValue().word()).isEqualTo("apple");
        assertThat(captor.getValue().partOfSpeech()).isEqualTo(PartOfSpeech.ADJECTIVE);
    }
}
