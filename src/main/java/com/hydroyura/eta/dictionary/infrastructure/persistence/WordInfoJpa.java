package com.hydroyura.eta.dictionary.infrastructure.persistence;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "word_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordInfoJpa {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", nullable = false)
    DictionaryJpa dictionary;

    @Column(name = "word_value", nullable = false)
    String wordValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech", nullable = false, length = 32)
    PartOfSpeech partOfSpeech;

    @Column(name = "target_repetitions", nullable = false)
    int targetRepetitions;

    @Column(name = "current_repetitions", nullable = false)
    int currentRepetitions;

    @Column(name = "reset_count", nullable = false)
    int resetCount;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "word_info_translation", joinColumns = @JoinColumn(name = "word_info_id"))
    @Column(name = "translation", nullable = false)
    Set<String> translations = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WordInfoJpa other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
