package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.IncrementWordRepetitions;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncrementWordRepetitionsUseCase implements IncrementWordRepetitions {

    private final DictionaryRepository dictionaryRepository;

    @Override
    public void execute(DictionaryId dictionaryId, Set<WordInfoId> wordInfoIds) {
        Objects.requireNonNull(dictionaryId, "dictionaryId must not be null");
        Objects.requireNonNull(wordInfoIds, "wordInfoIds must not be null");

        var dictionary = dictionaryRepository.findById(dictionaryId)
                .orElseThrow(() -> new DictionaryNotFoundException(dictionaryId));

        var wordsById = dictionary.getWordInfos().stream()
                .collect(Collectors.toMap(WordInfo::getId, Function.identity()));

        for (var wordInfoId : wordInfoIds) {
            var wordInfo = wordsById.get(wordInfoId);
            if (wordInfo == null) {
                log.warn("Skipping repetition increment for missing word {}", wordInfoId);
                continue;
            }
            wordInfo.incrementRepetition();
        }

        dictionaryRepository.save(dictionary);

        log.info("Incremented repetitions for {} words in dictionary {}", wordInfoIds.size(), dictionaryId);
    }
}
