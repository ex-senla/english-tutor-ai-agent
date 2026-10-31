package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WordFactory {

    private final WordSpecifications specifications;

    public Word create(String value) {
        var word = new Word(value);

        if (!specifications.validForCreation().isSatisfiedBy(word)) {
            throw new WordValidationException(value);
        }

        return word;
    }
}
