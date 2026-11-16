package com.hydroyura.eta.exercise.application.port;

/**
 * Вердикт LLM-валидации «Своей темы», возвращаемый адаптером.
 *
 * @param valid               осмысленная ли тема
 * @param canonicalName       каноническое название темы
 * @param matchedCatalogTopic ближайшая тема активного каталога (slug или {@code null})
 * @param reason              пояснение при невалидном вводе
 */
public record TopicValidationVerdict(
        boolean valid,
        String canonicalName,
        String matchedCatalogTopic,
        String reason
) {
}
