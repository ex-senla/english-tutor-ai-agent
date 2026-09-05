package com.hydroyura.eta.dictionary.domain.dictionary;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Optional;

public interface DictionaryRepository {

    Dictionary save(Dictionary dictionary);

    Optional<Dictionary> findById(DictionaryId id);
}
