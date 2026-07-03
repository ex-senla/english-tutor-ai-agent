package com.hydroyura.eta.dictionary.api.dictionary;

import com.hydroyura.eta.dictionary.api.word.WordProjection;
import java.util.Set;

public interface FindWords {

    Set<WordProjection> findByDictionaryId(DictionaryId dictionaryId);

    DictionaryStats getStats(DictionaryId dictionaryId);
}
