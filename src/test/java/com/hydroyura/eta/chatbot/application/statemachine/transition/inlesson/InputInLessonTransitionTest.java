package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordPosTranslations;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputInLessonTransitionTest {

    @Test
    void shouldMoveToConfirmationOnOneShotSuccess() {
        var checkWord = mock(CheckWord.class);
        var wordTranslator = mock(WordTranslator.class);
        when(checkWord.isValid(any())).thenReturn(true);
        when(wordTranslator.suggestPosAndTranslations(anyString())).thenReturn(
                new WordPosTranslations(PartOfSpeech.NOUN, List.of("яблоко", "фрукт")));
        var transition = new InputInLessonTransition(checkWord, wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.IN_LESSON);

        var result = transition.transit(chat, new Action.Input("apple"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_TRANSLATION_CHOICE);
        assertThat(chat.getContext().get(WORD_VALUE.getValue())).isEqualTo("apple");
        assertThat(chat.getContext().get(WORD_POS.getValue())).isEqualTo(PartOfSpeech.NOUN);
        assertThat((List<?>) chat.getContext().get(WORD_TRANSLATIONS.getValue())).hasSize(2);
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldNormalizeValueBeforeOneShotAndStorage() {
        var checkWord = mock(CheckWord.class);
        var wordTranslator = mock(WordTranslator.class);
        when(checkWord.isValid(any())).thenReturn(true);
        when(wordTranslator.suggestPosAndTranslations(anyString())).thenReturn(
                new WordPosTranslations(PartOfSpeech.EXPRESSION, List.of("ждать с нетерпением")));
        var transition = new InputInLessonTransition(checkWord, wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.IN_LESSON);

        transition.transit(chat, new Action.Input("  look   forward   to  "));

        assertThat(chat.getContext().get(WORD_VALUE.getValue())).isEqualTo("look forward to");
    }

    @Test
    void shouldFallbackToPosMenuOnOneShotFailure() {
        var checkWord = mock(CheckWord.class);
        var wordTranslator = mock(WordTranslator.class);
        when(checkWord.isValid(any())).thenReturn(true);
        when(wordTranslator.suggestPosAndTranslations(anyString())).thenReturn(
                new WordPosTranslations(null, List.of()));
        var transition = new InputInLessonTransition(checkWord, wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.IN_LESSON);

        var result = transition.transit(chat, new Action.Input("apple"));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_POS);
        assertThat(chat.getContext().get(WORD_VALUE.getValue())).isEqualTo("apple");
        assertThat(result).isInstanceOf(ActionResult.TextWithInlineKeyboard.class);
    }

    @Test
    void shouldStayInLessonOnInvalidWord() {
        var checkWord = mock(CheckWord.class);
        var wordTranslator = mock(WordTranslator.class);
        when(checkWord.isValid(any())).thenReturn(false);
        var transition = new InputInLessonTransition(checkWord, wordTranslator);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.IN_LESSON);

        var result = transition.transit(chat, new Action.Input("123!!"));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(chat.getContext()).doesNotContainKey(WORD_VALUE.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
    }
}
