package com.hydroyura.eta.dictionary.api.dictionary;

public record CheckLanguageCommand(
        String value,
        Language language
) {
}
