package com.hydroyura.eta.dictionary.infrastructure.ai;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordTranslationCommand;
import com.hydroyura.eta.dictionary.application.config.properties.WordInfoSpecificationProperties;
import com.hydroyura.eta.dictionary.application.config.properties.WordTranslationProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringAiWordTranslatorTest {

    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec callResponseSpec;

    private WordTranslationProperties properties;

    private SpringAiWordTranslator translator;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        properties = new WordTranslationProperties();
        translator = new SpringAiWordTranslator(chatClient, properties, new WordInfoSpecificationProperties());
    }

    @Test
    void shouldReturnValidTranslations() {
        stubResponse(new AiTranslationsResponse(List.of("яблоко", "фрукт", "яблочко")));

        var result = translator.translate(new WordTranslationCommand("apple", PartOfSpeech.NOUN));

        assertThat(result).containsExactly("яблоко", "фрукт", "яблочко");
    }

    @Test
    void shouldDiscardInvalidTranslations() {
        stubResponse(new AiTranslationsResponse(List.of("яблоко", "apple", "", "  ")));

        var result = translator.translate(new WordTranslationCommand("apple", PartOfSpeech.NOUN));

        assertThat(result).containsExactly("яблоко");
    }

    @Test
    void shouldDeduplicateTranslations() {
        stubResponse(new AiTranslationsResponse(List.of("дом", "дом", "здание")));

        var result = translator.translate(new WordTranslationCommand("house", PartOfSpeech.NOUN));

        assertThat(result).containsExactly("дом", "здание");
    }

    @Test
    void shouldLimitTranslationsToVariantCount() {
        properties.setVariantCount(2);
        stubResponse(new AiTranslationsResponse(List.of("яблоко", "фрукт", "яблочко")));

        var result = translator.translate(new WordTranslationCommand("apple", PartOfSpeech.NOUN));

        assertThat(result).containsExactly("яблоко", "фрукт");
    }

    @Test
    void shouldReturnEmptyListWhenChatClientThrows() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("AI down"));

        var result = translator.translate(new WordTranslationCommand("apple", PartOfSpeech.NOUN));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenResponseEmpty() {
        stubResponse(new AiTranslationsResponse(List.of()));

        var result = translator.translate(new WordTranslationCommand("apple", PartOfSpeech.NOUN));

        assertThat(result).isEmpty();
    }

    private void stubResponse(AiTranslationsResponse response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(AiTranslationsResponse.class)).thenReturn(response);
    }
}
