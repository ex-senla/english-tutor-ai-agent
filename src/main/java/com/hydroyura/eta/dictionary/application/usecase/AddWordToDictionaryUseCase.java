package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AddWordToDictionaryUseCase implements AddWordToDictionary {

    private final DictionaryRepository dictionaryRepository;

    private final WordInfoFactory wordInfoFactory;

    private final WordFactory wordFactory;

    @Override
    public WordInfoId execute(AddWordCommand cmd) {
        var dictionary = dictionaryRepository.findById(cmd.dictionaryId())
                .orElseThrow(() -> new DictionaryNotFoundException(cmd.dictionaryId()));

        var word = wordFactory.create(cmd.value());

        var wordInfo = wordInfoFactory.create(
                word,
                cmd.translations(),
                cmd.partOfSpeech()
        );

        dictionary.addWord(wordInfo);
        dictionaryRepository.save(dictionary);

        log.info("Word '{}' added to dictionary {}", cmd.value(), cmd.dictionaryId());
        return wordInfo.getId();
    }
}
