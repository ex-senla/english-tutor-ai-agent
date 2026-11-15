package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.dictionary.domain.word.exception.LastTranslationException;
import com.hydroyura.eta.dictionary.domain.word.exception.WordResetNotAllowedException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.hydroyura.eta.dictionary.api.word.WordInfoId.generate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordInfoTest {

    private WordInfo wordInfo(String value, Set<String> translations, PartOfSpeech partOfSpeech,
            Integer targetRepetitions, Integer currentRepetitions) {
        return wordInfo(value, translations, partOfSpeech, targetRepetitions, currentRepetitions, 0);
    }

    private WordInfo wordInfo(String value, Set<String> translations, PartOfSpeech partOfSpeech,
            Integer targetRepetitions, Integer currentRepetitions, Integer resetCount) {
        return new WordInfo(generate(), new Word(value), translations, partOfSpeech,
                targetRepetitions, currentRepetitions, resetCount);
    }

    @Test
    void shouldReturnNewStatusWhenRepetitionsAreZero() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        assertThat(word.getStatus()).isEqualTo(WordStatus.NEW);
    }

    @Test
    void shouldReturnInProgressWhenRepetitionsGreaterThanZero() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 5);
        assertThat(word.getStatus()).isEqualTo(WordStatus.IN_PROGRESS);
    }

    @Test
    void shouldReturnLearnedWhenRepetitionsEqualToTarget() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 10);
        assertThat(word.getStatus()).isEqualTo(WordStatus.LEARNED);
    }

    @Test
    void shouldReturnLearnedWhenRepetitionsGreaterThanTarget() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 15);
        assertThat(word.getStatus()).isEqualTo(WordStatus.LEARNED);
    }

    @Test
    void shouldReturnNewWhenRepetitionsAreNull() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, null, null);
        assertThat(word.getStatus()).isEqualTo(WordStatus.NEW);
    }

    @Test
    void shouldAddTranslation() {
        var word = wordInfo("run", new java.util.HashSet<>(Set.of("бежать")), PartOfSpeech.VERB, 10, 0);
        word.addTranslation("запускать");
        assertThat(word.getTranslations()).containsExactlyInAnyOrder("бежать", "запускать");
    }

    @Test
    void shouldRemoveTranslationWhenMoreThanOne() {
        var word = wordInfo("run", new java.util.HashSet<>(Set.of("бежать", "стартовать")),
                PartOfSpeech.VERB, 10, 0);
        word.removeTranslation("бежать");
        assertThat(word.getTranslations()).containsExactly("стартовать");
    }

    @Test
    void shouldNotRemoveLastTranslation() {
        var word = wordInfo("run", new java.util.HashSet<>(Set.of("бежать")), PartOfSpeech.VERB, 10, 0);
        assertThatThrownBy(() -> word.removeTranslation("бежать"))
                .isInstanceOf(LastTranslationException.class);
    }

    @Test
    void shouldSetPartOfSpeech() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        word.setPartOfSpeech(PartOfSpeech.ADJECTIVE);
        assertThat(word.getPartOfSpeech()).isEqualTo(PartOfSpeech.ADJECTIVE);
    }

    @Test
    void shouldNotSetNullPartOfSpeech() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        assertThatThrownBy(() -> word.setPartOfSpeech(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAddBlankTranslation() {
        var word = wordInfo("run", new java.util.HashSet<>(Set.of("бежать")), PartOfSpeech.VERB, 10, 0);
        assertThatThrownBy(() -> word.addTranslation("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldIncrementRepetitionFromNewToInProgress() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        word.incrementRepetition();
        assertThat(word.getCurrentRepetitions()).isEqualTo(1);
        assertThat(word.getStatus()).isEqualTo(WordStatus.IN_PROGRESS);
    }

    @Test
    void shouldIncrementRepetitionToLearned() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 9);
        word.incrementRepetition();
        assertThat(word.getCurrentRepetitions()).isEqualTo(10);
        assertThat(word.getStatus()).isEqualTo(WordStatus.LEARNED);
    }

    @Test
    void shouldStayLearnedWhenIncrementingBeyondTarget() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 10);
        word.incrementRepetition();
        assertThat(word.getCurrentRepetitions()).isEqualTo(11);
        assertThat(word.getStatus()).isEqualTo(WordStatus.LEARNED);
    }

    @Test
    void shouldResetLearnedWord() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 10, 1);
        word.resetLearning();
        assertThat(word.getCurrentRepetitions()).isZero();
        assertThat(word.getResetCount()).isEqualTo(2);
        assertThat(word.getStatus()).isEqualTo(WordStatus.NEW);
    }

    @Test
    void shouldRejectResetForNewWordWithoutMutation() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0, 0);
        assertThatThrownBy(word::resetLearning)
                .isInstanceOf(WordResetNotAllowedException.class);
        assertThat(word.getCurrentRepetitions()).isZero();
        assertThat(word.getResetCount()).isZero();
    }

    @Test
    void shouldRejectResetForInProgressWordWithoutMutation() {
        var word = wordInfo("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 5, 0);
        assertThatThrownBy(word::resetLearning)
                .isInstanceOf(WordResetNotAllowedException.class);
        assertThat(word.getCurrentRepetitions()).isEqualTo(5);
        assertThat(word.getResetCount()).isZero();
    }
}
