package com.hydroyura.eta.dictionary.domain.dictionary;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordSpecificationConfig;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DictionaryTest {

    private WordFactory wordFactory;

    @BeforeEach
    void setUp() {
        var config = new WordSpecificationConfig() {

            @Override
            public String valueAllowedPattern() {
                return "[a-zA-Z'\\- ]+";
            }

            @Override
            public int valueMinLength() {
                return 1;
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
        wordFactory = new WordFactory(specs, config);
    }

    @Test
    void shouldAddWord() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");
        var word = wordFactory.create("apple", Set.of("яблоко"), PartOfSpeech.NOUN);

        dictionary.addWord(word);

        assertThat(dictionary.getWords()).containsExactly(word);
    }

    @Test
    void shouldRejectDuplicateWordByValue() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");
        dictionary.addWord(wordFactory.create("apple", Set.of("яблоко"), PartOfSpeech.NOUN));

        assertThatThrownBy(() -> dictionary.addWord(
                wordFactory.create("APPLE", Set.of("яблочко"), PartOfSpeech.NOUN)))
                .isInstanceOf(WordAlreadyExistsException.class);
    }

    @Test
    void shouldRejectNullWord() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");

        assertThatThrownBy(() -> dictionary.addWord(null))
                .isInstanceOf(NullPointerException.class);
    }
}
