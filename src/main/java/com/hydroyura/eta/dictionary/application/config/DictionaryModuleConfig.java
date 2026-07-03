package com.hydroyura.eta.dictionary.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.application.config.properties.WordSpecificationSpringProperties;
import com.hydroyura.eta.dictionary.application.usecase.AddWordToDictionaryUseCase;
import com.hydroyura.eta.dictionary.application.usecase.CreateDictionaryUseCase;
import com.hydroyura.eta.dictionary.application.usecase.FindWordsService;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordSpecificationConfig;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WordSpecificationSpringProperties.class)
public class DictionaryModuleConfig {

    @Bean
    WordSpecifications wordSpecifications(WordSpecificationConfig config) {
        return new WordSpecifications(config);
    }

    @Bean
    WordFactory wordFactory(WordSpecifications specifications, WordSpecificationConfig config) {
        return new WordFactory(specifications, config);
    }

    @Bean
    AddWordToDictionary addWordToDictionary(DictionaryRepository repository, WordFactory factory) {
        return new AddWordToDictionaryUseCase(repository, factory);
    }

    @Bean
    CreateDictionary createDictionary(DictionaryRepository repository) {
        return new CreateDictionaryUseCase(repository);
    }

    @Bean
    FindWords findWords(DictionaryRepository repository) {
        return new FindWordsService(repository);
    }
}
