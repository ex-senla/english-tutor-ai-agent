package com.hydroyura.eta.chatbot.application.statemachine.handler;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ChatStateHandlerCoverageTest extends PostgresIntegrationTestBase {

    @Autowired
    private Map<ChatState, Handler> handlers;

    @Test
    void everyChatStateShouldHaveHandler() {
        for (var state : ChatState.values()) {
            assertThat(handlers.get(state))
                    .as("Handler for state %s", state)
                    .isNotNull();
        }
    }
}
