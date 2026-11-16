package com.hydroyura.eta.exercise.api.exercise;

/**
 * Тема упражнения: предустановленная тема каталога (по стабильному slug) или
 * «своя тема» (канонический свободный текст после LLM-валидации).
 */
public sealed interface ExerciseTopic permits CatalogTopic, CustomTopic {
}
