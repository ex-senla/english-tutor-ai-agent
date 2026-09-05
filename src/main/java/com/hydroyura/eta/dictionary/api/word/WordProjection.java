package com.hydroyura.eta.dictionary.api.word;

import java.util.Set;

public record WordProjection(
        WordId id,
        String value,
        Set<String> translations,
        PartOfSpeech partOfSpeech
) {
}
