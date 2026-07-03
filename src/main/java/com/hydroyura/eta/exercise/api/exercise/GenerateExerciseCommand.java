package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Objects;

public record GenerateExerciseCommand(
    ExerciseType type,
    String topic,
    DictionaryId dictionaryId
) {
    public GenerateExerciseCommand {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(dictionaryId, "dictionaryId must not be null");
    }
}
