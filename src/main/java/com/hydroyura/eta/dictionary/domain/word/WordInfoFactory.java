package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@RequiredArgsConstructor
public final class WordInfoFactory {

    private final WordInfoSpecifications specifications;

    private final int defaultTargetRepetitions;

    public WordInfo create(
            Word word,
            Set<String> translations,
            PartOfSpeech partOfSpeech
    ) {
        return create(word, translations, partOfSpeech, defaultTargetRepetitions);
    }

    public WordInfo create(
            Word word,
            Set<String> translations,
            PartOfSpeech partOfSpeech,
            int targetRepetitions
    ) {
        var wordInfo = new WordInfo(
                WordInfoId.generate(),
                word,
                Set.copyOf(translations),
                partOfSpeech,
                targetRepetitions,
                0
        );

        if (!specifications.validForCreation().isSatisfiedBy(wordInfo)) {
            throw new WordValidationException(word.getValue());
        }

        return wordInfo;
    }
}
