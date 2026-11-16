package com.hydroyura.eta.exercise.api.exercise;

import java.util.Objects;

/**
 * «Своя тема» — свободный текст, прошедший LLM-валидацию (или принятый
 * fail-open при сбое LLM).
 *
 * @param name каноническое название темы.
 */
public record CustomTopic(String name) implements ExerciseTopic {

    public CustomTopic {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
