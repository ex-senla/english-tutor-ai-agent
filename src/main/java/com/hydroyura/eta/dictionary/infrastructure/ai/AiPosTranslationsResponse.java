package com.hydroyura.eta.dictionary.infrastructure.ai;

import java.util.List;

/**
 * Structured one-shot response: part of speech + translations.
 */
public record AiPosTranslationsResponse(String partOfSpeech, List<String> translations) {
}
