package com.hydroyura.eta.dictionary.api.dictionary;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;

public interface AddWordToDictionary {

    WordInfoId execute(AddWordCommand command);
}
