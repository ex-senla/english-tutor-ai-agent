package com.hydroyura.eta.exercise.infrastructure.ai;

import java.util.List;

/**
 * One exercise item: a sentence/question with a blank and its expected answer.
 */
public record AiExerciseItem(
        String sentence,
        List<String> options,
        String correctAnswer
) {
}
