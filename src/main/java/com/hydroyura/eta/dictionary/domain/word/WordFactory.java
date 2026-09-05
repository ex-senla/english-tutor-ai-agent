package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@RequiredArgsConstructor
public final class WordFactory {

    private final WordSpecifications specifications;

    private final WordSpecificationConfig config;

    public Word create(
            String value,
            Set<String> translations,
            PartOfSpeech partOfSpeech
    ) {
        return create(value, translations, partOfSpeech, config.defaultTargetRepetitions());
    }

    public Word create(
            String value,
            Set<String> translations,
            PartOfSpeech partOfSpeech,
            int targetRepetitions
    ) {
        var word = new Word(
                WordId.generate(),
                value,
                Set.copyOf(translations),
                partOfSpeech,
                targetRepetitions,
                0
        );

        if (!specifications.validForCreation().isSatisfiedBy(word)) {
            throw new WordValidationException(value);
        }

        return word;
    }
}
