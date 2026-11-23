package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.shared.api.DomainException;

/**
 * Узкое доменное исключение генерации упражнения: AI-сервис недоступен (сеть/таймаут/5xx)
 * либо вернул невалидный или пустой ответ. Находится в {@code exercise.api.exercise}, чтобы
 * быть доступным модулям exercise (бросает) и chatbot (ловит) без нарушения границ Modulith.
 */
public class ExerciseGenerationException extends DomainException {

    public ExerciseGenerationException(String message) {
        super(message);
    }

    public ExerciseGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
