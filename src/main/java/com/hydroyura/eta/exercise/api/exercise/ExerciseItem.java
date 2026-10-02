package com.hydroyura.eta.exercise.api.exercise;

import java.util.List;
import java.util.Objects;

public record ExerciseItem(
        String sentence,
        List<String> options,
        String correctAnswer
) {

    public ExerciseItem {
        Objects.requireNonNull(sentence, "sentence must not be null");
        Objects.requireNonNull(correctAnswer, "correctAnswer must not be null");
        options = options == null ? List.of() : List.copyOf(options);
    }
}
