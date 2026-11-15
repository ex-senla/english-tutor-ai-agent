package com.hydroyura.eta.dictionary.domain.word.exception;

import com.hydroyura.eta.dictionary.api.word.WordStatus;

public class WordResetNotAllowedException extends WordDomainException {

    public WordResetNotAllowedException(WordStatus status) {
        super("Learning reset is not allowed for word with status " + status);
    }
}
