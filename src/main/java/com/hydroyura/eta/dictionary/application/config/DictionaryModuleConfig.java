package com.hydroyura.eta.dictionary.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.CheckWord;
import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.application.config.properties.WordInfoSpecificationProperties;
import com.hydroyura.eta.dictionary.application.usecase.AddWordToDictionaryUseCase;
import com.hydroyura.eta.dictionary.application.usecase.CheckWordUseCase;
import com.hydroyura.eta.dictionary.application.usecase.CreateDictionaryUseCase;
import com.hydroyura.eta.dictionary.application.usecase.FindWordsService;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties(WordInfoSpecificationProperties.class)
public class DictionaryModuleConfig {

    @Bean
    WordSpecifications wordSpecifications(WordInfoSpecificationProperties properties) {
        return new WordSpecifications(
                Pattern.compile(properties.getValueAllowedPattern()),
                properties.getValueMinLength(),
                properties.getValueMaxLength()
        );
    }

    @Bean
    WordInfoSpecifications wordInfoSpecifications(WordInfoSpecificationProperties properties) {
        return new WordInfoSpecifications(
                Pattern.compile(properties.getTranslationAllowedPattern()),
                properties.getTranslationMinLength(),
                properties.getTranslationMaxLength(),
                properties.getMinTranslations(),
                properties.getMaxTargetRepetitions()
        );
    }

    @Bean
    WordFactory wordFactory(WordSpecifications specifications) {
        return new WordFactory(specifications);
    }

    @Bean
    WordInfoFactory wordInfoFactory(WordInfoSpecifications specifications, WordInfoSpecificationProperties properties) {
        return new WordInfoFactory(specifications, properties.getDefaultTargetRepetitions());
    }

    @Bean
    AddWordToDictionary addWordToDictionary(DictionaryRepository repository, WordInfoFactory wordInfoFactory,
            WordFactory wordFactory) {
        return new AddWordToDictionaryUseCase(repository, wordInfoFactory, wordFactory);
    }

    @Bean
    CheckWord checkWord(WordFactory wordFactory) {
        return new CheckWordUseCase(wordFactory);
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
