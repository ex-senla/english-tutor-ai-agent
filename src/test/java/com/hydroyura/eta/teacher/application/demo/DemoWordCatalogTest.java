package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordSpecificationConfig;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DemoWordCatalogTest {

    private WordFactory factory;

    @BeforeEach
    void setUp() {
        WordSpecificationConfig config = new WordSpecificationConfig() {

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
        factory = new WordFactory(new WordSpecifications(config), config);
    }

    @Test
    void everyCatalogWordShouldPassValidation() {
        for (var word : DemoWordCatalog.words()) {
            assertThatCode(() -> factory.create(word.value(), word.translations(), word.partOfSpeech()))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void catalogShouldCoverDefaultWordRange() {
        assertThat(DemoWordCatalog.words()).hasSizeGreaterThanOrEqualTo(5);
    }
}
