package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordFactoryTest {

    private WordFactory factory;

    @BeforeEach
    void setUp() {
        var config = new WordSpecificationConfig() {

            @Override
            public String valueAllowedPattern() {
                return "[a-zA-Z'\\- ]+";
            }

            @Override
            public int valueMinLength() {
                return 2;
            }

            @Override
            public int valueMaxLength() {
                return 50;
            }

            @Override
            public String translationAllowedPattern() {
                return "[а-яА-ЯёЁ ]+";
            }

            @Override
            public int translationMinLength() {
                return 1;
            }

            @Override
            public int translationMaxLength() {
                return 100;
            }

            @Override
            public int minTranslations() {
                return 1;
            }

            @Override
            public int maxTargetRepetitions() {
                return 100;
            }

            @Override
            public int defaultTargetRepetitions() {
                return 10;
            }
        };
        var specs = new WordSpecifications(config);
        factory = new WordFactory(specs, config);
    }

    @Test
    void shouldCreateValidWord() {
        var word = factory.create("apple", Set.of("яблоко"), PartOfSpeech.NOUN);

        assertThat(word.getId()).isNotNull();
        assertThat(word.getValue()).isEqualTo("apple");
        assertThat(word.getTranslations()).containsExactly("яблоко");
        assertThat(word.getPartOfSpeech()).isEqualTo(PartOfSpeech.NOUN);
        assertThat(word.getTargetRepetitions()).isEqualTo(10); // default from config
        assertThat(word.getCurrentRepetitions()).isEqualTo(0);
    }

    @Test
    void shouldCreateWordWithCustomTargetRepetitions() {
        var word = factory.create("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 20);

        assertThat(word.getTargetRepetitions()).isEqualTo(20);
    }

    @Test
    void shouldRejectRussianValue() {
        assertThatThrownBy(() -> factory.create("привет", Set.of("hello"), PartOfSpeech.NOUN))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectEmptyTranslations() {
        assertThatThrownBy(() -> factory.create("apple", Set.of(), PartOfSpeech.NOUN))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectTooShortValue() {
        assertThatThrownBy(() -> factory.create("a", Set.of("яблоко"), PartOfSpeech.NOUN))
                .isInstanceOf(WordValidationException.class);
    }
}
