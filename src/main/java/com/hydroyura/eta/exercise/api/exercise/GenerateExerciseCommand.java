package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Objects;

public record GenerateExerciseCommand(
        ExerciseType type,
        String grammarRule,
        String topic,
        DictionaryId dictionaryId,
        String cefrLevel
) {

    public GenerateExerciseCommand {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(grammarRule, "grammarRule must not be null");
        if (grammarRule.isBlank()) {
            throw new IllegalArgumentException("grammarRule must not be blank");
        }
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(dictionaryId, "dictionaryId must not be null");
        Objects.requireNonNull(cefrLevel, "cefrLevel must not be null");
        if (cefrLevel.isBlank()) {
            throw new IllegalArgumentException("cefrLevel must not be blank");
        }
    }
}
