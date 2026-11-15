package com.hydroyura.eta.dictionary.api.dictionary;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import java.util.Set;

public interface IncrementWordRepetitions {

    void execute(DictionaryId dictionaryId, Set<WordInfoId> wordInfoIds);
}
