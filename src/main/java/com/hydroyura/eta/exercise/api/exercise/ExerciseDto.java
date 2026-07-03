package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseType;

public record ExerciseDto(
    ExerciseId id,
    ExerciseType type,
    String topic,
    String content,
    порчExerciseStatus status
) {}
