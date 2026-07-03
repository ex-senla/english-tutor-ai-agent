package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class FindWordsService implements FindWords {

    private final DictionaryRepository dictionaryRepository;

    @Override
    public Set<WordProjection> findByDictionaryId(DictionaryId dictionaryId) {
        var dictionary = dictionaryRepository.findById(dictionaryId)
            .orElseThrow(() -> new DictionaryNotFoundException(dictionaryId));

        return dictionary.getWords().stream()
            .map(w -> new WordProjection(w.getId(), w.getValue(), w.getTranslations(), w.getPartOfSpeech()))
            .collect(Collectors.toSet());
    }
}
