package com.hydroyura.eta.dictionary.domain.dictionary;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DictionaryTest {

    private WordFactory wordFactory;

    private WordInfoFactory wordInfoFactory;

    @BeforeEach
    void setUp() {
        wordFactory = new WordFactory(new WordSpecifications(
                Pattern.compile("[a-zA-Z'\\- ]+"), /* valueMinLength */ 1, /* valueMaxLength */ 50));
        wordInfoFactory = new WordInfoFactory(new WordInfoSpecifications(
                Pattern.compile("[а-яА-ЯёЁ\\- ]+"), /* translationMinLength */ 1,
                /* translationMaxLength */ 100, /* minTranslations */ 1,
                /* maxTargetRepetitions */ 100), /* defaultTargetRepetitions */ 10);
    }

    private WordInfo wordInfo(String value, String translation, PartOfSpeech partOfSpeech) {
        return wordInfoFactory.create(wordFactory.create(value), Set.of(translation), partOfSpeech);
    }

    @Test
    void shouldAddWord() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");
        var word = wordInfo("apple", "яблоко", PartOfSpeech.NOUN);

        dictionary.addWord(word);

        assertThat(dictionary.getWordInfos()).containsExactly(word);
    }

    @Test
    void shouldRejectDuplicateWordByValue() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");
        dictionary.addWord(wordInfo("apple", "яблоко", PartOfSpeech.NOUN));

        assertThatThrownBy(() -> dictionary.addWord(wordInfo("APPLE", "яблочко", PartOfSpeech.NOUN)))
                .isInstanceOf(WordAlreadyExistsException.class);
    }

    @Test
    void shouldRejectNullWord() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");

        assertThatThrownBy(() -> dictionary.addWord(null))
                .isInstanceOf(NullPointerException.class);
    }
}
