package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AddWordToDictionaryUseCase implements AddWordToDictionary {

    private final DictionaryRepository dictionaryRepository;

    private final WordFactory wordFactory;

    @Override
    public WordId execute(AddWordCommand cmd) {
        var dictionary = dictionaryRepository.findById(cmd.dictionaryId())
                .orElseThrow(() -> new DictionaryNotFoundException(cmd.dictionaryId()));

        var word = wordFactory.create(
                cmd.value(),
                cmd.translations(),
                cmd.partOfSpeech()
        );

        dictionary.addWord(word);
        dictionaryRepository.save(dictionary);

        log.info("Word '{}' added to dictionary {}", cmd.value(), cmd.dictionaryId());
        return word.getId();
    }
}
