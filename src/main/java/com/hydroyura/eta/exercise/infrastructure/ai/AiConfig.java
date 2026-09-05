package com.hydroyura.eta.exercise.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(
                        "You are a professional English tutor AI. Respond only with valid JSON objects, no other text.")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
