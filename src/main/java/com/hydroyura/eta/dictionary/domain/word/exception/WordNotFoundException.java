package com.hydroyura.eta.dictionary.domain.word.exception;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;

public class WordNotFoundException extends WordDomainException {

    public WordNotFoundException(WordInfoId id) {
        super("Word not found: " + id.value());
    }
}
