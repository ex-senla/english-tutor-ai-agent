package com.hydroyura.eta.exercise.application.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseTopicsPropertiesTest {

    @Test
    void shouldHaveDefaultPath() {
        var properties = new ExerciseTopicsProperties();

        assertThat(properties.getPath()).isEqualTo("/exercise/topics/grammar-topics.json");
    }

    @Test
    void shouldOverridePath() {
        var properties = new ExerciseTopicsProperties();
        properties.setPath("/custom/topics.json");

        assertThat(properties.getPath()).isEqualTo("/custom/topics.json");
    }
}
