package com.hydroyura.eta.exercise.api.exercise;

public record CheckExerciseResult(
    boolean correct,
    String feedback,
    ExerciseDto exercise
) {}
