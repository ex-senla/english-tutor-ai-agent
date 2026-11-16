package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.exercise.api.exercise.TopicValidationResult;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopic;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopicCommand;
import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.TopicValidationVerdict;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM-валидация свободного ввода «Своей темы»: детерминированные пре-проверки,
 * один LLM-вызов, верификация совпадения с активным каталогом (анти-галлюцинации)
 * и fail-open при сбое адаптера.
 */
@Slf4j
@RequiredArgsConstructor
public class ValidateCustomTopicUseCase implements ValidateCustomTopic {

    private static final int MAX_TOPIC_LENGTH = 100;

    private final CustomTopicValidator validator;

    private final GrammarTopicCatalog catalog;

    @Override
    public TopicValidationResult validate(ValidateCustomTopicCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        var raw = command.rawTopic();
        var trimmed = raw.trim();

        if (trimmed.isEmpty()) {
            return new TopicValidationResult(true, false, trimmed, null, "Тема не может быть пустой");
        }
        if (trimmed.length() > MAX_TOPIC_LENGTH) {
            return new TopicValidationResult(true, false, trimmed, null,
                    "Тема слишком длинная (максимум %d символов)".formatted(MAX_TOPIC_LENGTH));
        }

        Optional<TopicValidationVerdict> verdict;
        try {
            verdict = validator.validate(trimmed);
        } catch (Exception e) {
            log.warn("Custom topic validation failed, accepting input as-is: {}", trimmed, e);
            return failOpen(trimmed);
        }

        if (verdict.isEmpty()) {
            log.warn("Custom topic validator returned no verdict, accepting input as-is: {}", trimmed);
            return failOpen(trimmed);
        }

        var v = verdict.get();
        var canonicalName = v.canonicalName() == null || v.canonicalName().isBlank()
                ? trimmed
                : v.canonicalName().trim();

        var matchedSlug = v.matchedCatalogTopic();
        if (matchedSlug != null && !matchedSlug.isBlank() && !catalog.exists(matchedSlug)) {
            log.warn("LLM returned non-active/non-existent catalog topic '{}', ignoring it", matchedSlug);
            matchedSlug = null;
        }

        return new TopicValidationResult(true, v.valid(), canonicalName, matchedSlug, v.reason());
    }

    private TopicValidationResult failOpen(String trimmed) {
        return new TopicValidationResult(false, true, trimmed, null, null);
    }
}
