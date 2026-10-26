package com.hydroyura.eta.dictionary.api.dictionary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported languages in ISO 639-1.
 */
@Getter
@RequiredArgsConstructor
public enum Language {

    ENGLISH("en"),
    RUSSIAN("ru");

    private final String value;

}
