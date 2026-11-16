package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WordFactory {

    private final WordSpecifications specifications;

    public Word create(String value) {
        var normalized = normalize(value);
        var word = new Word(normalized);

        if (!specifications.validForCreation().isSatisfiedBy(word)) {
            throw new WordValidationException(normalized);
        }

        return word;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
