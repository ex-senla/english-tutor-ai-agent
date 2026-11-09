package com.hydroyura.eta.dictionary.domain.word;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class WordSpecificationsTest {

    private WordSpecifications specs;

    @BeforeEach
    void setUp() {
        specs = new WordSpecifications(
                Pattern.compile("[a-zA-Z'\\- ]+"),
                /* valueMinLength */ 2,
                /* valueMaxLength */ 50);
    }

    @Test
    void validForCreationShouldPassForValidWord() {
        assertThat(specs.validForCreation().isSatisfiedBy(new Word("apple"))).isTrue();
    }

    @Test
    void shouldRejectBlankValue() {
        assertThat(specs.valueNotBlank().isSatisfiedBy(new Word("  "))).isFalse();
    }

    @Test
    void shouldRejectRussianValue() {
        assertThat(specs.valueMatchesPattern().isSatisfiedBy(new Word("привет"))).isFalse();
    }

    @Test
    void shouldRejectTooShortValue() {
        assertThat(specs.valueLengthInRange().isSatisfiedBy(new Word("a"))).isFalse();
    }

    @Test
    void shouldAcceptPhrasalVerb() {
        assertThat(specs.valueMatchesPattern().isSatisfiedBy(new Word("look after"))).isTrue();
    }

    @Test
    void shouldAcceptApostrophe() {
        assertThat(specs.valueMatchesPattern().isSatisfiedBy(new Word("don't"))).isTrue();
    }
}
