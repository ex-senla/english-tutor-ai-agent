package com.hydroyura.eta.dictionary.domain.dictionary;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Objects;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import java.util.Objects;
import com.hydroyura.eta.dictionary.domain.word.Word;
import java.util.Objects;
import lombok.AllArgsConstructor;
import java.util.Objects;
import lombok.Getter;
import java.util.Objects;
import org.jmolecules.ddd.annotation.AggregateRoot;
import java.util.Objects;
import org.jmolecules.ddd.annotation.Identity;
import java.util.Objects;

import java.util.Set;
import java.util.Objects;

@Getter
@AggregateRoot
@AllArgsConstructor
public final class Dictionary {

    @Identity
    private final DictionaryId id;

    private final Set<Word> words;

    private String name;

    public void addWord(Word word) {
        Objects.requireNonNull(word, "Word must not be null");
        if (words.stream().anyMatch(w -> w.getValue().equalsIgnoreCase(word.getValue()))) {
            throw new WordAlreadyExistsException(word.getValue());
        }
        this.words.add(word);
    }
}
