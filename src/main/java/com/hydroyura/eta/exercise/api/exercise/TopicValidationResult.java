package com.hydroyura.eta.exercise.api.exercise;

/**
 * Результат валидации «Своей темы».
 *
 * @param validated     {@code true}, если валидатор отработал (LLM вернул вердикт либо сработала
 *                      детерминированная пре-проверка); {@code false} — сбой LLM (fail-open).
 * @param valid         осмысленная ли тема (для fail-open — {@code true}).
 * @param canonicalName каноническое название темы (для fail-open — обрезанный ввод).
 * @param matchedSlug   совпавшая активная тема каталога (может быть {@code null}).
 * @param reason        пояснение при невалидном вводе.
 */
public record TopicValidationResult(
        boolean validated,
        boolean valid,
        String canonicalName,
        String matchedSlug,
        String reason
) {
}
