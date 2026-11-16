package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.TopicValidationVerdict;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * LLM-адаптер валидации «Своей темы» на Spring AI.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiTopicValidator implements CustomTopicValidator {

    private static final String PROMPT_PATH = "/exercise/prompts/topic-validation-system.txt";

    private final ChatClient chatClient;

    private final GrammarTopicCatalog catalog;

    @Override
    public Optional<TopicValidationVerdict> validate(String rawTopic) {
        var systemPrompt = loadPrompt();
        var userMessage = buildUserMessage(rawTopic);

        AiTopicValidationResponse response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .entity(AiTopicValidationResponse.class);
        } catch (Exception e) {
            log.error("AI topic validation failed for '{}'", rawTopic, e);
            return Optional.empty();
        }

        if (response == null) {
            log.warn("AI topic validation returned null for '{}'", rawTopic);
            return Optional.empty();
        }

        var canonicalName = response.canonicalName() == null ? "" : response.canonicalName().trim();
        var matchedSlug = response.matchedCatalogTopic();
        if (matchedSlug != null && matchedSlug.isBlank()) {
            matchedSlug = null;
        }

        return Optional.of(new TopicValidationVerdict(
                Boolean.TRUE.equals(response.valid()),
                canonicalName,
                matchedSlug,
                response.reason()));
    }

    private String buildUserMessage(String rawTopic) {
        var active = catalog.activeTopics().stream()
                .map(topic -> topic.slug() + " — " + topic.name())
                .collect(Collectors.joining("\n"));
        return "Raw topic: " + rawTopic
                + "\n\nAvailable ACTIVE catalog topics (slug — name):\n" + active;
    }

    private static String loadPrompt() {
        try (var in = SpringAiTopicValidator.class.getResourceAsStream(PROMPT_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Prompt resource not found: " + PROMPT_PATH);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt: " + PROMPT_PATH, e);
        }
    }
}
