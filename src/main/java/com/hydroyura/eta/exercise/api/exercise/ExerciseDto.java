package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;

public record ExerciseDto(
        ExerciseId id,
        ExerciseType type,
        String topic,
        String content,
        String expectedAnswer,
        ExerciseStatus status
) {
}
