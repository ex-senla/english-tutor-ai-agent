package com.hydroyura.eta.exercise.application.port;

import java.util.Optional;

/**
 * Порт LLM-валидации свободного ввода «Своей темы».
 */
public interface CustomTopicValidator {

    /**
     * @return вердикт валидатора либо {@link Optional#empty()} при сбое/пустом ответе LLM.
     */
    Optional<TopicValidationVerdict> validate(String rawTopic);
}
