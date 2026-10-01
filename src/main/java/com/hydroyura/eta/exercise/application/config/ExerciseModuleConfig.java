package com.hydroyura.eta.exercise.application.config;

import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.exercise.application.config.properties.ExerciseGenerationProperties;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.usecase.CheckExerciseUseCase;
import com.hydroyura.eta.exercise.application.usecase.GenerateExerciseUseCase;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExerciseGenerationProperties.class)
public class ExerciseModuleConfig {

    @Bean
    GenerateExerciseUseCase generateExerciseUseCase(ExerciseRepository repository,
            ExerciseGenerator generator,
            FindWords findWords) {
        return new GenerateExerciseUseCase(repository, generator, findWords);
    }

    @Bean
    CheckExerciseUseCase checkExerciseUseCase(ExerciseRepository repository) {
        return new CheckExerciseUseCase(repository);
    }
}
