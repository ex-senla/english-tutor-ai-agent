package com.hydroyura.eta.dictionary.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.application.config.properties.WordSpecificationSpringProperties;
import com.hydroyura.eta.dictionary.application.usecase.AddWordToDictionaryUseCase;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DictionaryModuleConfigTest.TestConfig.class)
class DictionaryModuleConfigTest {

    @Configuration
    @EnableConfigurationProperties(WordSpecificationSpringProperties.class)
    static class TestConfig {

        @Bean
        WordSpecifications wordSpecifications(WordSpecificationSpringProperties config) {
            return new WordSpecifications(config);
        }

        @Bean
        WordFactory wordFactory(WordSpecifications specs, WordSpecificationSpringProperties config) {
            return new WordFactory(specs, config);
        }

        @Bean
        DictionaryRepository dictionaryRepository() {
            return new DictionaryRepository() {

                private final java.util.Map<DictionaryId, Dictionary> store = new java.util.HashMap<>();

                @Override
                public Dictionary save(Dictionary dict) {
                    store.put(dict.getId(), dict);
                    return dict;
                }

                @Override
                public java.util.Optional<Dictionary> findById(DictionaryId id) {
                    return java.util.Optional.ofNullable(store.get(id));
                }
            };
        }

        @Bean
        AddWordToDictionary addWordToDictionary(DictionaryRepository repo, WordFactory factory) {
            return new AddWordToDictionaryUseCase(repo, factory);
        }
    }

    @Autowired
    private WordSpecifications wordSpecifications;

    @Autowired
    private WordFactory wordFactory;

    @Autowired
    private WordSpecificationSpringProperties properties;

    @Autowired
    private AddWordToDictionary addWordToDictionary;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Test
    void shouldWireWordSpecifications() {
        assertThat(wordSpecifications).isNotNull();
    }

    @Test
    void shouldWireWordFactory() {
        assertThat(wordFactory).isNotNull();
    }

    @Test
    void shouldBindPropertiesWithDefaults() {
        assertThat(properties.defaultTargetRepetitions()).isEqualTo(10);
        assertThat(properties.valueMinLength()).isEqualTo(1);
        assertThat(properties.maxTargetRepetitions()).isEqualTo(100);
    }

    @Test
    void factoryShouldCreateWord() {
        var word = wordFactory.create("hello", java.util.Set.of("привет"),
                com.hydroyura.eta.dictionary.api.word.PartOfSpeech.NOUN);

        assertThat(word).isNotNull();
        assertThat(word.getValue()).isEqualTo("hello");
        assertThat(word.getTargetRepetitions()).isEqualTo(10);
    }

    @Test
    void shouldAddWordThroughUseCase() {
        var dictId = DictionaryId.generate();
        dictionaryRepository.save(new Dictionary(dictId, new HashSet<>(), "Test"));

        addWordToDictionary.execute(new AddWordCommand(dictId, "hello", Set.of("привет"), PartOfSpeech.NOUN));

        var dict = dictionaryRepository.findById(dictId).orElseThrow();
        assertThat(dict.getWords()).hasSize(1);
        assertThat(dict.getWords().iterator().next().getValue()).isEqualTo("hello");
    }
}
