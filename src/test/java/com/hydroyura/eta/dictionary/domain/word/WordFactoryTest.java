package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordFactoryTest {

    private WordFactory factory;

    @BeforeEach
    void setUp() {
        var specs = new WordSpecifications(
                Pattern.compile("[a-zA-Z'\\- ]+"),
                /* valueMinLength */ 2,
                /* valueMaxLength */ 50);
        factory = new WordFactory(specs);
    }

    @Test
    void shouldCreateValidWord() {
        var word = factory.create("apple");

        assertThat(word.getValue()).isEqualTo("apple");
    }

    @Test
    void shouldRejectRussianValue() {
        assertThatThrownBy(() -> factory.create("привет"))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectTooShortValue() {
        assertThatThrownBy(() -> factory.create("a"))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> factory.create("  "))
                .isInstanceOf(WordValidationException.class);
    }
}
