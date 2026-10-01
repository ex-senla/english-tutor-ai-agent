package com.hydroyura.eta.exercise.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eta.exercise.generation")
public class ExerciseGenerationProperties {

    private int sentenceCount = 5;
}
