package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DemoWordInfoCatalogTest {

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

    @Test
    void everyCatalogWordShouldPassValidation() {
        for (var word : DemoWordCatalog.words()) {
            assertThatCode(() -> wordInfoFactory.create(
                    wordFactory.create(word.value()), word.translations(), word.partOfSpeech()))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void catalogShouldCoverDefaultWordRange() {
        assertThat(DemoWordCatalog.words()).hasSizeGreaterThanOrEqualTo(5);
    }
}
