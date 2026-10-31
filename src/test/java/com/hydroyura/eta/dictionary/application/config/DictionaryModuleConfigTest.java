package com.hydroyura.eta.dictionary.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.application.config.properties.WordInfoSpecificationProperties;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DictionaryModuleConfigTest.TestConfig.class)
class DictionaryModuleConfigTest {

    @Configuration
    @Import(DictionaryModuleConfig.class)
    static class TestConfig {

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
    }

    @Autowired
    private WordInfoSpecifications wordInfoSpecifications;

    @Autowired
    private WordInfoFactory wordInfoFactory;

    @Autowired
    private WordFactory wordFactory;

    @Autowired
    private WordInfoSpecificationProperties properties;

    @Autowired
    private AddWordToDictionary addWordToDictionary;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Test
    void shouldWireWordSpecifications() {
        assertThat(wordInfoSpecifications).isNotNull();
    }

    @Test
    void shouldWireWordFactory() {
        assertThat(wordInfoFactory).isNotNull();
        assertThat(wordFactory).isNotNull();
    }

    @Test
    void shouldBindPropertiesWithDefaults() {
        assertThat(properties.getDefaultTargetRepetitions()).isEqualTo(10);
        assertThat(properties.getValueMinLength()).isEqualTo(1);
        assertThat(properties.getMaxTargetRepetitions()).isEqualTo(100);
    }

    @Test
    void factoryShouldCreateWord() {
        var word = wordFactory.create("hello");
        var wordInfo = wordInfoFactory.create(word, Set.of("привет"), PartOfSpeech.NOUN);

        assertThat(wordInfo).isNotNull();
        assertThat(wordInfo.getWord().getValue()).isEqualTo("hello");
        assertThat(wordInfo.getTargetRepetitions()).isEqualTo(10);
    }

    @Test
    void shouldAddWordThroughUseCase() {
        var dictId = DictionaryId.generate();
        dictionaryRepository.save(new Dictionary(dictId, new HashSet<>(), "Test"));

        addWordToDictionary.execute(new AddWordCommand(dictId, "hello", Set.of("привет"), PartOfSpeech.NOUN));

        var dict = dictionaryRepository.findById(dictId).orElseThrow();
        assertThat(dict.getWordInfos()).hasSize(1);
        assertThat(dict.getWordInfos().iterator().next().getWord().getValue()).isEqualTo("hello");
    }
}
