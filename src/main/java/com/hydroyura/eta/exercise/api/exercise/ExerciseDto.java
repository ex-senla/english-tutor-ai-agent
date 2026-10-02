package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.util.List;

public record ExerciseDto(
        ExerciseId id,
        ExerciseType type,
        String topic,
        String content,
        List<String> expectedAnswers,
        List<ExerciseItem> items,
        ExerciseStatus status
) {
}
