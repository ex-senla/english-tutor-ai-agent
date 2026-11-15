package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import java.util.Objects;
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

        return dictionary.getWordInfos().stream()
                .map(w -> new WordProjection(w.getId(), w.getWord().value(), w.getTranslations(), w.getPartOfSpeech(), w
                        .getStatus()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<WordProjection> findByDictionaryIdAndWordIds(DictionaryId dictionaryId, Set<WordInfoId> wordInfoIds) {
        Objects.requireNonNull(wordInfoIds, "wordInfoIds must not be null");

        var dictionary = dictionaryRepository.findById(dictionaryId)
                .orElseThrow(() -> new DictionaryNotFoundException(dictionaryId));

        if (wordInfoIds.isEmpty()) {
            return Set.of();
        }

        return dictionary.getWordInfos().stream()
                .filter(w -> wordInfoIds.contains(w.getId()))
                .map(w -> new WordProjection(w.getId(), w.getWord().value(), w.getTranslations(), w.getPartOfSpeech(), w
                        .getStatus()))
                .collect(Collectors.toSet());
    }

    @Override
    public DictionaryStats getStats(DictionaryId dictionaryId) {
        var words = dictionaryRepository.findById(dictionaryId)
                .orElseThrow(() -> new DictionaryNotFoundException(dictionaryId))
                .getWordInfos();

        var total = words.size();
        var newCount = words.stream().filter(w -> w.getStatus() == WordStatus.NEW).count();
        var inProgress = words.stream().filter(w -> w.getStatus() == WordStatus.IN_PROGRESS).count();
        var learned = words.stream().filter(w -> w.getStatus() == WordStatus.LEARNED).count();

        return new DictionaryStats(total, newCount, inProgress, learned);
    }
}
