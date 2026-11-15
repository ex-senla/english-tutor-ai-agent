package com.hydroyura.eta.chatbot.application.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotSearchPropertiesTest {

    @Test
    void shouldHaveDefaultSuggestionsLimit() {
        var properties = new ChatbotSearchProperties();

        assertThat(properties.getSuggestionsLimit()).isEqualTo(10);
    }

    @Test
    void shouldOverrideSuggestionsLimit() {
        var properties = new ChatbotSearchProperties();
        properties.setSuggestionsLimit(5);

        assertThat(properties.getSuggestionsLimit()).isEqualTo(5);
    }
}
