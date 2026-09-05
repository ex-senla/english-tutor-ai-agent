package com.hydroyura.eta.dictionary.api.dictionary;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import java.util.Set;

public record AddWordCommand(
        DictionaryId dictionaryId,
        String value,
        Set<String> translations,
        PartOfSpeech partOfSpeech
) {
}
