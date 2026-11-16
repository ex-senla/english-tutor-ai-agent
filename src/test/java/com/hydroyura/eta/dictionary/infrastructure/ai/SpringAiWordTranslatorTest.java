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

    @Test
    void shouldSuggestNounForSingleWord() {
        stubPosResponse(new AiPosTranslationsResponse("noun", List.of("яблоко", "фрукт")));

        var result = translator.suggestPosAndTranslations("apple");

        assertThat(result.partOfSpeech()).isEqualTo(PartOfSpeech.NOUN);
        assertThat(result.translations()).containsExactly("яблоко", "фрукт");
    }

    @Test
    void shouldSuggestExpressionForMultiword() {
        stubPosResponse(new AiPosTranslationsResponse("expression", List.of("ждать с нетерпением")));

        var result = translator.suggestPosAndTranslations("look forward to");

        assertThat(result.partOfSpeech()).isEqualTo(PartOfSpeech.EXPRESSION);
    }

    @Test
    void shouldReturnNullPosForUnknownPartOfSpeech() {
        stubPosResponse(new AiPosTranslationsResponse("adverb", List.of("перевод")));

        var result = translator.suggestPosAndTranslations("quickly");

        assertThat(result.partOfSpeech()).isNull();
    }

    @Test
    void shouldReturnNullPosWhenNoValidTranslations() {
        stubPosResponse(new AiPosTranslationsResponse("noun", List.of("apple", "123", "")));

        var result = translator.suggestPosAndTranslations("apple");

        assertThat(result.partOfSpeech()).isNull();
    }

    @Test
    void shouldDeduplicateAndLimitOneShotTranslations() {
        properties.setVariantCount(2);
        stubPosResponse(new AiPosTranslationsResponse("noun", List.of("дом", "дом", "здание", "крыша")));

        var result = translator.suggestPosAndTranslations("house");

        assertThat(result.translations()).containsExactly("дом", "здание");
    }

    @Test
    void shouldReturnNullPosWhenOneShotChatClientThrows() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("AI down"));

        var result = translator.suggestPosAndTranslations("apple");

        assertThat(result.partOfSpeech()).isNull();
    }

    private void stubPosResponse(AiPosTranslationsResponse response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(AiPosTranslationsResponse.class)).thenReturn(response);
    }

    private void stubResponse(AiTranslationsResponse response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(AiTranslationsResponse.class)).thenReturn(response);
    }
}
