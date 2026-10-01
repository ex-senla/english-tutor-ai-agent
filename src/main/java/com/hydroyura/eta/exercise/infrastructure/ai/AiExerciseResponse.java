package com.hydroyura.eta.exercise.infrastructure.ai;

import java.util.List;

/**
 * Structured response from the AI model for exercise generation.
 * Spring AI uses Jackson 3 to deserialize this from the LLM JSON response.
 */
public record AiExerciseResponse(List<AiExerciseItem> items) {
}
