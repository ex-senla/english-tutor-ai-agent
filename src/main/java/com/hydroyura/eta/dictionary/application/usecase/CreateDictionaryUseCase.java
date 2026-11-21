package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionaryCommand;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateDictionaryUseCase implements CreateDictionary {

    private final DictionaryRepository dictionaryRepository;

    @Override
    public DictionaryId execute(CreateDictionaryCommand cmd) {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), cmd.name());
        dictionary = dictionaryRepository.save(dictionary);

        log.info("Dictionary '{}' created: {}", cmd.name(), dictionary.getId().value());
        return dictionary.getId();
    }
}
