package com.hydroyura.eta.exercise.api.exercise;

import java.util.Objects;

/**
 * Предустановленная тема каталога грамматических тем.
 *
 * @param slug стабильный уникальный идентификатор темы (lowercase-hyphenated).
 */
public record CatalogTopic(String slug) implements ExerciseTopic {

    public CatalogTopic {
        Objects.requireNonNull(slug, "slug must not be null");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug must not be blank");
        }
    }
}
