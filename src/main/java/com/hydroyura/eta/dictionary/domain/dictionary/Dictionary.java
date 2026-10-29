package com.hydroyura.eta.dictionary.domain.dictionary;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Objects;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Set;

@Getter
@AggregateRoot
@AllArgsConstructor
public final class Dictionary {

    @Identity
    private final DictionaryId id;

    private final Set<WordInfo> wordInfos;

    private String name;

    public void addWord(WordInfo wordInfo) {
        Objects.requireNonNull(wordInfo, "Word must not be null");
        if (wordInfos.stream().anyMatch(w -> w.getWord().getValue().equalsIgnoreCase(wordInfo.getWord().getValue()))) {
            throw new WordAlreadyExistsException(wordInfo.getWord().getValue());
        }
        this.wordInfos.add(wordInfo);
    }
}
