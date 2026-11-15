package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Pattern;

import static com.hydroyura.eta.dictionary.api.word.WordInfoId.generate;
import static org.assertj.core.api.Assertions.assertThat;

class WordInfoSpecificationsTest {

    private WordInfoSpecifications specs;

    @BeforeEach
    void setUp() {
        specs = new WordInfoSpecifications(
                Pattern.compile("[а-яА-ЯёЁ\\- ]+"),
                /* translationMinLength */ 1,
                /* translationMaxLength */ 100,
                /* minTranslations */ 1,
                /* maxTargetRepetitions */ 100);
    }

    private WordInfo validWordInfo() {
        return new WordInfo(generate(), new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
    }

    @Test
    void validForCreationShouldPassForValidWordInfo() {
        assertThat(specs.validForCreation().isSatisfiedBy(validWordInfo())).isTrue();
    }

    @Test
    void shouldRejectNullTranslations() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), null, PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.translationsNotNull().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectEmptyTranslations() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of(), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.translationsNotEmpty().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectEnglishTranslation() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("hello"), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.translationsMatchPattern().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldAcceptTranslationWithHyphen() {
        var wordInfo = new WordInfo(generate(), new Word("half"), Set.of("пол-"), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.translationsMatchPattern().isSatisfiedBy(wordInfo)).isTrue();
    }

    @Test
    void shouldRejectBlankTranslation() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("  "), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.eachTranslationNotBlank().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectTooLongTranslation() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("я".repeat(101)), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.eachTranslationLengthInRange().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectMissingPartOfSpeech() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("яблоко"), null, 10, 0);
        assertThat(specs.partOfSpeechNotNull().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectTargetRepetitionsExceedingMax() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN, 200, 0);
        assertThat(specs.targetRepetitionsNotExceedingMax().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectZeroTargetRepetitions() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN, 0, 0);
        assertThat(specs.targetRepetitionsPositive().isSatisfiedBy(wordInfo)).isFalse();
    }

    @Test
    void shouldRejectNegativeCurrentRepetitions() {
        var wordInfo = new WordInfo(generate(), new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN, 10, -1);
        assertThat(specs.currentRepetitionsNotNegative().isSatisfiedBy(wordInfo)).isFalse();
    }
}
