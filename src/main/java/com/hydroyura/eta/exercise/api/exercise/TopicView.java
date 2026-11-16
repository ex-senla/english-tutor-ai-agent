package com.hydroyura.eta.exercise.api.exercise;

/**
 * Read-проекция темы каталога для чат-бота.
 *
 * @param slug              стабильный идентификатор темы
 * @param name              английское название
 * @param nameRu            русское название (опционально)
 * @param hasActiveChildren есть ли активные дочерние темы
 */
public record TopicView(String slug, String name, String nameRu, boolean hasActiveChildren) {
}
