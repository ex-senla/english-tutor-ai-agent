package com.hydroyura.eta.dictionary.infrastructure.ai;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordPosTranslations;
import com.hydroyura.eta.dictionary.api.word.WordTranslationCommand;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.dictionary.application.config.properties.WordInfoSpecificationProperties;
import com.hydroyura.eta.dictionary.application.config.properties.WordTranslationProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiWordTranslator implements WordTranslator {

    private static final String PROMPT_PATH = "/dictionary/prompts/word-translations-system.txt";

    private static final String ONE_SHOT_PROMPT_PATH = "/dictionary/prompts/word-pos-translations-system.txt";

    private final ChatClient chatClient;

    private final WordTranslationProperties properties;

    private final WordInfoSpecificationProperties specificationProperties;

    @Override
    public List<String> translate(WordTranslationCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.word(), "word must not be null");
        Objects.requireNonNull(command.partOfSpeech(), "partOfSpeech must not be null");

        var count = properties.getVariantCount();
        var systemPrompt = loadPrompt(PROMPT_PATH).replace("{count}", String.valueOf(count));
        var userMessage = "Word: " + command.word() + "\nPart of speech: " + posLabel(command.partOfSpeech());

        AiTranslationsResponse response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .entity(AiTranslationsResponse.class);
        } catch (Exception e) {
            log.error("AI word translation failed for word '{}'", command.word(), e);
            return List.of();
        }

        if (response == null || response.translations() == null || response.translations().isEmpty()) {
            log.warn("AI returned empty translations for word '{}'", command.word());
            return List.of();
        }

        return validate(response.translations());
    }

    @Override
    public WordPosTranslations suggestPosAndTranslations(String word) {
        Objects.requireNonNull(word, "word must not be null");

        var count = properties.getVariantCount();
        var systemPrompt = loadPrompt(ONE_SHOT_PROMPT_PATH).replace("{count}", String.valueOf(count));
        var userMessage = "Word: " + word;

        AiPosTranslationsResponse response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .entity(AiPosTranslationsResponse.class);
        } catch (Exception e) {
            log.error("AI one-shot POS/translations failed for word '{}'", word, e);
            return failure();
        }

        if (response == null || response.partOfSpeech() == null) {
            log.warn("AI returned empty one-shot response for word '{}'", word);
            return failure();
        }

        var pos = mapPartOfSpeech(response.partOfSpeech());
        if (pos == null) {
            log.warn("AI returned unknown part of speech '{}' for word '{}'", response.partOfSpeech(), word);
            return failure();
        }

        var translations = response.translations() == null ? List.<String>of() : validate(response.translations());
        if (translations.isEmpty()) {
            log.warn("AI returned no valid translations for word '{}'", word);
            return failure();
        }
        return new WordPosTranslations(pos, translations);
    }

    private WordPosTranslations failure() {
        return new WordPosTranslations(null, List.of());
    }

    private PartOfSpeech mapPartOfSpeech(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "noun" -> PartOfSpeech.NOUN;
            case "verb" -> PartOfSpeech.VERB;
            case "adjective" -> PartOfSpeech.ADJECTIVE;
            case "expression" -> PartOfSpeech.EXPRESSION;
            default -> null;
        };
    }

    private List<String> validate(List<String> raw) {
        var pattern = Pattern.compile(specificationProperties.getTranslationAllowedPattern());
        var minLength = specificationProperties.getTranslationMinLength();
        var maxLength = specificationProperties.getTranslationMaxLength();
        var limit = properties.getVariantCount();

        var result = new LinkedHashSet<String>();
        for (var translation : raw) {
            if (translation == null || translation.isBlank()) {
                continue;
            }
            var trimmed = translation.trim();
            if (trimmed.length() < minLength || trimmed.length() > maxLength) {
                continue;
            }
            if (!pattern.matcher(trimmed).matches()) {
                continue;
            }
            result.add(trimmed);
            if (result.size() >= limit) {
                break;
            }
        }
        return List.copyOf(result);
    }

    private static String loadPrompt(String path) {
        try (var in = SpringAiWordTranslator.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Prompt resource not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt: " + path, e);
        }
    }

    private static String posLabel(PartOfSpeech partOfSpeech) {
        return switch (partOfSpeech) {
            case NOUN -> "noun";
            case VERB -> "verb";
            case ADJECTIVE -> "adjective";
            case EXPRESSION -> "expression";
        };
    }
}
