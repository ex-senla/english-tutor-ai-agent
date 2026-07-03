package com.hydroyura.eta.exercise.api.exercise;

import java.util.Objects;

public record CheckExerciseCommand(
    ExerciseId exerciseId,
    String userAnswer
) {
    public CheckExerciseCommand {
        Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        Objects.requireNonNull(userAnswer, "userAnswer must not be null");
    }
}
