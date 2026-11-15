package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.ResetWordLearning;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.word.exception.WordNotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ResetWordLearningUseCase implements ResetWordLearning {

    private final DictionaryRepository dictionaryRepository;

    @Override
    public void execute(DictionaryId dictionaryId, WordInfoId wordInfoId) {
        Objects.requireNonNull(dictionaryId, "dictionaryId must not be null");
        Objects.requireNonNull(wordInfoId, "wordInfoId must not be null");

        var dictionary = dictionaryRepository.findById(dictionaryId)
                .orElseThrow(() -> new DictionaryNotFoundException(dictionaryId));

        var wordInfo = dictionary.getWordInfos().stream()
                .filter(word -> word.getId().equals(wordInfoId))
                .findFirst()
                .orElseThrow(() -> new WordNotFoundException(wordInfoId));

        wordInfo.resetLearning();
        dictionaryRepository.save(dictionary);

        log.info("Learning reset for word {} in dictionary {}", wordInfoId, dictionaryId);
    }
}
