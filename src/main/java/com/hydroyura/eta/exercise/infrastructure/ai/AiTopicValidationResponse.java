package com.hydroyura.eta.exercise.infrastructure.ai;

/**
 * Structured response from the AI model for custom topic validation.
 */
public record AiTopicValidationResponse(
        Boolean valid,
        String canonicalName,
        String matchedCatalogTopic,
        String reason
) {
}
