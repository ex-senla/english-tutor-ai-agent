package com.hydroyura.eta.exercise.domain.topic;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Узел иерархического каталога грамматических тем.
 *
 * @param slug        стабильный уникальный идентификатор (lowercase-hyphenated)
 * @param parentSlug  ссылка на родителя ({@code null} для корня)
 * @param name        английское название
 * @param nameRu      русское название (опционально)
 * @param description краткое описание (опционально)
 * @param sortOrder   порядок среди соседних узлов
 * @param active      признак доступности в v1
 * @param paid        признак платности (в v1 не фильтрует)
 */
@ValueObject
public record GrammarTopic(
        String slug,
        String parentSlug,
        String name,
        String nameRu,
        String description,
        int sortOrder,
        boolean active,
        boolean paid
) {
}
