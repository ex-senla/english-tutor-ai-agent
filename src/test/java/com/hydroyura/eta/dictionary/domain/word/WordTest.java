package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.domain.word.exception.LastTranslationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordTest {

    @Test
    void shouldReturnNewStatusWhenRepetitionsAreZero() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        assertThat(word.getStatus()).isEqualTo(WordStatus.NEW);
    }

    @Test
    void shouldReturnInProgressWhenRepetitionsGreaterThanZero() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 5);
        assertThat(word.getStatus()).isEqualTo(WordStatus.IN_PROGRESS);
    }

    @Test
    void shouldReturnLearnedWhenRepetitionsEqualToTarget() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 10);
        assertThat(word.getStatus()).isEqualTo(WordStatus.LEARNED);
    }

    @Test
    void shouldReturnLearnedWhenRepetitionsGreaterThanTarget() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 15);
        assertThat(word.getStatus()).isEqualTo(WordStatus.LEARNED);
    }

    @Test
    void shouldReturnNewWhenRepetitionsAreNull() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, null, null);
        assertThat(word.getStatus()).isEqualTo(WordStatus.NEW);
    }

    @Test
    void shouldAddTranslation() {
        var word = new Word(WordId.generate(), "run", new java.util.HashSet<>(Set.of("бежать")), PartOfSpeech.VERB, 10,
                0);
        word.addTranslation("запускать");
        assertThat(word.getTranslations()).containsExactlyInAnyOrder("бежать", "запускать");
    }

    @Test
    void shouldRemoveTranslationWhenMoreThanOne() {
        var word = new Word(WordId.generate(), "run", new java.util.HashSet<>(Set.of("бежать", "стартовать")),
                PartOfSpeech.VERB, 10, 0);
        word.removeTranslation("бежать");
        assertThat(word.getTranslations()).containsExactly("стартовать");
    }

    @Test
    void shouldNotRemoveLastTranslation() {
        var word = new Word(WordId.generate(), "run", new java.util.HashSet<>(Set.of("бежать")), PartOfSpeech.VERB, 10,
                0);
        assertThatThrownBy(() -> word.removeTranslation("бежать"))
                .isInstanceOf(LastTranslationException.class);
    }

    @Test
    void shouldSetPartOfSpeech() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        word.setPartOfSpeech(PartOfSpeech.ADJECTIVE);
        assertThat(word.getPartOfSpeech()).isEqualTo(PartOfSpeech.ADJECTIVE);
    }

    @Test
    void shouldNotSetNullPartOfSpeech() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        assertThatThrownBy(() -> word.setPartOfSpeech(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAddBlankTranslation() {
        var word = new Word(WordId.generate(), "run", new java.util.HashSet<>(Set.of("бежать")), PartOfSpeech.VERB, 10,
                0);
        assertThatThrownBy(() -> word.addTranslation("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
