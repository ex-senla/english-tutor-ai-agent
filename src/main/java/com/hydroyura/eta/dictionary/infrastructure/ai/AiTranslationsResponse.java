package com.hydroyura.eta.dictionary.infrastructure.ai;

import java.util.List;

/**
 * Structured response from the AI model for word translation.
 * Spring AI uses Jackson to deserialize this from the LLM JSON response.
 */
public record AiTranslationsResponse(List<String> translations) {
}
