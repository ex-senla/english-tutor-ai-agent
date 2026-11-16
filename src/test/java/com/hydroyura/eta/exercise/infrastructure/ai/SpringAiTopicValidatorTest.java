package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringAiTopicValidatorTest {

    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec callResponseSpec;

    private SpringAiTopicValidator validator;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        validator = new SpringAiTopicValidator(chatClient,
                new GrammarTopicCatalog("/exercise/topics/test-grammar-topics.json"));
    }

    @Test
    void shouldMapVerdict() {
        stubResponse(new AiTopicValidationResponse(true, "Past Simple", "past-simple", null));

        var result = validator.validate("past simple");

        assertThat(result).isPresent();
        assertThat(result.get().valid()).isTrue();
        assertThat(result.get().canonicalName()).isEqualTo("Past Simple");
        assertThat(result.get().matchedCatalogTopic()).isEqualTo("past-simple");
    }

    @Test
    void shouldReturnEmptyOnChatClientFailure() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("AI down"));

        assertThat(validator.validate("past simple")).isEmpty();
    }

    @Test
    void shouldReturnEmptyOnNullResponse() {
        stubResponse(null);

        assertThat(validator.validate("past simple")).isEmpty();
    }

    private void stubResponse(AiTopicValidationResponse response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(AiTopicValidationResponse.class)).thenReturn(response);
    }
}
