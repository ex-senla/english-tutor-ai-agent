package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordInfoFactoryTest {

    private WordInfoFactory factory;

    @BeforeEach
    void setUp() {
        var specs = new WordInfoSpecifications(
                Pattern.compile("[а-яА-ЯёЁ\\- ]+"),
                /* translationMinLength */ 1,
                /* translationMaxLength */ 100,
                /* minTranslations */ 1,
                /* maxTargetRepetitions */ 100);
        factory = new WordInfoFactory(specs, /* defaultTargetRepetitions */ 10);
    }

    @Test
    void shouldCreateValidWordInfo() {
        var wordInfo = factory.create(new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN);

        assertThat(wordInfo.getId()).isNotNull();
        assertThat(wordInfo.getWord().getValue()).isEqualTo("apple");
        assertThat(wordInfo.getTranslations()).containsExactly("яблоко");
        assertThat(wordInfo.getPartOfSpeech()).isEqualTo(PartOfSpeech.NOUN);
        assertThat(wordInfo.getTargetRepetitions()).isEqualTo(10); // default from config
        assertThat(wordInfo.getCurrentRepetitions()).isEqualTo(0);
    }

    @Test
    void shouldCreateWordInfoWithCustomTargetRepetitions() {
        var wordInfo = factory.create(new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN, 20);

        assertThat(wordInfo.getTargetRepetitions()).isEqualTo(20);
    }

    @Test
    void shouldRejectEmptyTranslations() {
        assertThatThrownBy(() -> factory.create(new Word("apple"), Set.of(), PartOfSpeech.NOUN))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectEnglishTranslation() {
        assertThatThrownBy(() -> factory.create(new Word("apple"), Set.of("hello"), PartOfSpeech.NOUN))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectNullPartOfSpeech() {
        assertThatThrownBy(() -> factory.create(new Word("apple"), Set.of("яблоко"), null))
                .isInstanceOf(WordValidationException.class);
    }

    @Test
    void shouldRejectTargetRepetitionsExceedingMax() {
        assertThatThrownBy(() -> factory.create(new Word("apple"), Set.of("яблоко"), PartOfSpeech.NOUN, 200))
                .isInstanceOf(WordValidationException.class);
    }
}
