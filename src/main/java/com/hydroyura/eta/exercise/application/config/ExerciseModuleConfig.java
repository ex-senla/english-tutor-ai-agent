package com.hydroyura.eta.exercise.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.IncrementWordRepetitions;
import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopic;
import com.hydroyura.eta.exercise.application.config.properties.ExerciseGenerationProperties;
import com.hydroyura.eta.exercise.application.config.properties.ExerciseTopicsProperties;
import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import com.hydroyura.eta.exercise.application.usecase.CheckExerciseUseCase;
import com.hydroyura.eta.exercise.application.usecase.GenerateExerciseUseCase;
import com.hydroyura.eta.exercise.application.usecase.ValidateCustomTopicUseCase;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ExerciseGenerationProperties.class,
        ExerciseTopicsProperties.class})
public class ExerciseModuleConfig {

    @Bean
    GrammarTopicCatalog grammarTopicCatalog(ExerciseTopicsProperties properties) {
        return new GrammarTopicCatalog(properties.getPath());
    }

    @Bean
    GenerateExerciseUseCase generateExerciseUseCase(ExerciseRepository repository,
            ExerciseGenerator generator,
            FindWords findWords,
            IncrementWordRepetitions incrementWordRepetitions,
            GrammarTopicCatalog topicCatalog) {
        return new GenerateExerciseUseCase(repository, generator, findWords, incrementWordRepetitions, topicCatalog);
    }

    @Bean
    ValidateCustomTopic validateCustomTopic(CustomTopicValidator validator, GrammarTopicCatalog topicCatalog) {
        return new ValidateCustomTopicUseCase(validator, topicCatalog);
    }

    @Bean
    CheckExerciseUseCase checkExerciseUseCase(ExerciseRepository repository) {
        return new CheckExerciseUseCase(repository);
    }
}
