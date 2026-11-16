package com.hydroyura.eta.chatbot.application.statemachine.transition.awaitingtranslationchoice;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_POS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_TRANSLATIONS;
import static com.hydroyura.eta.chatbot.domain.chat.ContextKey.WORD_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

class PosChangeCbAwaitingTranslationChoiceTransitionTest {

    @Test
    void shouldReturnToPosMenuAndClearPosAndTranslations() {
        var transition = new PosChangeCbAwaitingTranslationChoiceTransition();
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_TRANSLATION_CHOICE);
        chat.getContext().put(WORD_VALUE.getValue(), "apple");
        chat.getContext().put(WORD_POS.getValue(), PartOfSpeech.NOUN);
        chat.getContext().put(WORD_TRANSLATIONS.getValue(), List.of("яблоко"));

        var result = transition.transit(chat, new Action.Callback("poschange", "", 42));

        assertThat(chat.getState()).isEqualTo(ChatState.AWAITING_POS);
        assertThat(chat.getContext()).doesNotContainKeys(WORD_POS.getValue(), WORD_TRANSLATIONS.getValue());
        assertThat(chat.getContext()).containsKey(WORD_VALUE.getValue());
        assertThat(result).isInstanceOf(ActionResult.EditMessageText.class);
    }
}
