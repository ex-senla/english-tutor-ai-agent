package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.ChatService;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hydroyura.eta.chatbot.view.Messages.GENERATING_EXERCISE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EnglishTutorBotIndicatorTest {

    private EnglishTutorBot bot;

    @BeforeEach
    void setUp() {
        bot = new EnglishTutorBot("test-token", "test-bot", mock(ChatService.class), mock(UpdateParser.class),
                mock(SendMessageConverter.class), mock(StateMachine.class));
    }

    @Test
    void shouldNotShowIndicatorOnThematicTopicInput() {
        var chat = chat(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);

        assertThat(bot.indicatorFor(chat, new Action.Input("Travel"))).isNull();
    }

    @Test
    void shouldNotShowIndicatorOnThematicTopicSkipCallback() {
        var chat = chat(ChatState.AWAITING_EXERCISE_THEMATIC_TOPIC);

        assertThat(bot.indicatorFor(chat, new Action.Callback("topicskip", "", 1))).isNull();
    }

    @Test
    void shouldShowGenerationIndicatorOnWordSelectionDone() {
        var chat = chat(ChatState.AWAITING_EXERCISE_WORD_SELECTION);

        assertThat(bot.indicatorFor(chat, new Action.Callback("exdone", "", 1))).isEqualTo(GENERATING_EXERCISE);
    }

    private Chat chat(ChatState state) {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(state);
        return chat;
    }
}
