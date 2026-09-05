package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WordSpecificationsTest {

    private WordSpecifications specs;

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
        specs = new WordSpecifications(config);
    }

    private Word validWord() {
        return new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
    }

    @Test
    void validForCreationShouldPassForValidWord() {
        assertThat(specs.validForCreation().isSatisfiedBy(validWord())).isTrue();
    }

    @Test
    void shouldRejectBlankValue() {
        var word = new Word(WordId.generate(), "  ", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.valueNotBlank().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldRejectRussianValue() {
        var word = new Word(WordId.generate(), "привет", Set.of("hello"), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.valueMatchesPattern().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldRejectTooShortValue() {
        var word = new Word(WordId.generate(), "a", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.valueLengthInRange().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldRejectEmptyTranslations() {
        var word = new Word(WordId.generate(), "apple", Set.of(), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.translationsNotEmpty().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldRejectEnglishTranslation() {
        var word = new Word(WordId.generate(), "apple", Set.of("apple"), PartOfSpeech.NOUN, 10, 0);
        assertThat(specs.translationsMatchPattern().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldRejectTargetRepetitionsExceedingMax() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 200, 0);
        assertThat(specs.targetRepetitionsNotExceedingMax().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldRejectNegativeCurrentRepetitions() {
        var word = new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, -1);
        assertThat(specs.currentRepetitionsNotNegative().isSatisfiedBy(word)).isFalse();
    }

    @Test
    void shouldAcceptPhrasalVerb() {
        var word = new Word(WordId.generate(), "look after", Set.of("присматривать"), PartOfSpeech.VERB, 10, 0);
        assertThat(specs.valueMatchesPattern().isSatisfiedBy(word)).isTrue();
    }

    @Test
    void shouldAcceptApostrophe() {
        var word = new Word(WordId.generate(), "don't", Set.of("не делать"), PartOfSpeech.VERB, 10, 0);
        assertThat(specs.valueMatchesPattern().isSatisfiedBy(word)).isTrue();
    }
}
