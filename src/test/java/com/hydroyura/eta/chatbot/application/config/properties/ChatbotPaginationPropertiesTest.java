package com.hydroyura.eta.chatbot.application.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotPaginationPropertiesTest {

    @Test
    void shouldHaveDefaultPageSize() {
        var properties = new ChatbotPaginationProperties();

        assertThat(properties.getPageSize()).isEqualTo(8);
    }

    @Test
    void shouldOverridePageSize() {
        var properties = new ChatbotPaginationProperties();
        properties.setPageSize(5);

        assertThat(properties.getPageSize()).isEqualTo(5);
    }
}
