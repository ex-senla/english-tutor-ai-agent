package com.hydroyura.eta.dictionary.api.dictionary;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;

public interface ResetWordLearning {

    void execute(DictionaryId dictionaryId, WordInfoId wordInfoId);
}
