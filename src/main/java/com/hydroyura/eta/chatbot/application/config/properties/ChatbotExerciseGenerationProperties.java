package com.hydroyura.eta.chatbot.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eta.chatbot.exercise-generation")
public class ChatbotExerciseGenerationProperties {

    private String errorMessage =
            "⚠️ Не удалось сгенерировать упражнение. Попробуйте, пожалуйста, ещё раз немного позже.";
}
