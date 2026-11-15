package com.hydroyura.eta.chatbot.application.statemachine.transition.inlesson;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputInLessonTransitionTest {

    @Test
    void shouldMoveToPosOnValidWord() {
        var checkWord = mock(CheckWord.class);
        when(checkWord.isValid(any())).thenReturn(true);
        var transition = new InputInLessonTransition(checkWord);

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
        when(checkWord.isValid(any())).thenReturn(false);
        var transition = new InputInLessonTransition(checkWord);

        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.IN_LESSON);

        var result = transition.transit(chat, new Action.Input("123!!"));

        assertThat(chat.getState()).isEqualTo(ChatState.IN_LESSON);
        assertThat(chat.getContext()).doesNotContainKey(WORD_VALUE.getValue());
        assertThat(result).isInstanceOf(ActionResult.TextResponse.class);
    }
}
