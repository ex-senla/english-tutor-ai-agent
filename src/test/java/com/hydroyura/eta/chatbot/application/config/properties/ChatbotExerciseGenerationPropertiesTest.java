package com.hydroyura.eta.chatbot.application.config.properties;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotExerciseGenerationPropertiesTest {

    private static final String DEFAULT_ERROR_MESSAGE =
            "⚠️ Не удалось сгенерировать упражнение. Попробуйте, пожалуйста, ещё раз немного позже.";

    @Test
    void shouldHaveDefaultErrorMessage() {
        var properties = new ChatbotExerciseGenerationProperties();

        assertThat(properties.getErrorMessage()).isEqualTo(DEFAULT_ERROR_MESSAGE);
    }

    @Test
    void shouldBindErrorMessageWithRelaxedBinding() {
        var source = new MapConfigurationPropertySource(Map.of(
                "eta.chatbot.exercise-generation.error-message", "Мой текст"));

        var properties = new Binder(source)
                .bind("eta.chatbot.exercise-generation", Bindable.of(ChatbotExerciseGenerationProperties.class))
                .get();

        assertThat(properties.getErrorMessage()).isEqualTo("Мой текст");
    }
}
