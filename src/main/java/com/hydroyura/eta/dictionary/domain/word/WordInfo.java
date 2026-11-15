package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import java.util.Objects;
import com.hydroyura.eta.dictionary.domain.word.exception.LastTranslationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Set;

@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class WordInfo {

    @Identity
    private final WordInfoId id;

    private final Word word;

    private final Set<String> translations;

    private PartOfSpeech partOfSpeech;

    private Integer targetRepetitions;

    private Integer currentRepetitions;

    public WordStatus getStatus() {
        if (currentRepetitions == null || targetRepetitions == null) {
            return WordStatus.NEW;
        }
        if (currentRepetitions >= targetRepetitions) {
            return WordStatus.LEARNED;
        }
        if (currentRepetitions > 0) {
            return WordStatus.IN_PROGRESS;
        }
        return WordStatus.NEW;
    }

    public void addTranslation(String translation) {
        Objects.requireNonNull(translation, "Translation must not be null");
        if (translation.isBlank()) {
            throw new IllegalArgumentException("Translation must not be blank");
        }
        this.translations.add(translation);
    }

    public void removeTranslation(String translation) {
        if (this.translations.size() <= 1) {
            throw new LastTranslationException();
        }
        this.translations.remove(translation);
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        Objects.requireNonNull(partOfSpeech, "PartOfSpeech must not be null");
        this.partOfSpeech = partOfSpeech;
    }
}
