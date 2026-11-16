package com.hydroyura.eta.exercise.api.exercise;

import java.util.Objects;

/**
 * Команда LLM-валидации свободного ввода «Своей темы».
 *
 * @param rawTopic сырой ввод преподавателя.
 */
public record ValidateCustomTopicCommand(String rawTopic) {

    public ValidateCustomTopicCommand {
        Objects.requireNonNull(rawTopic, "rawTopic must not be null");
    }
}
