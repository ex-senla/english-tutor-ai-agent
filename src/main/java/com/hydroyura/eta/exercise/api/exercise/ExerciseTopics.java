package com.hydroyura.eta.exercise.api.exercise;

import java.util.List;
import java.util.Optional;

/**
 * Read-порт каталога грамматических тем для других модулей (chatbot).
 * Отдаёт только активное v1-подмножество тем.
 */
public interface ExerciseTopics {

    List<TopicView> roots();

    List<TopicView> children(String slug);

    List<TopicView> search(String query);

    boolean exists(String slug);

    Optional<TopicView> find(String slug);

    Optional<TopicView> parent(String slug);

    String path(String slug);
}
