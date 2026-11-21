package com.hydroyura.eta.exercise.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.util.List;
import java.util.stream.Collectors;

final class ExerciseJpaMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    ExerciseJpa toEntity(Exercise domain) {
        var entity = new ExerciseJpa();
        entity.id = domain.getId().value();
        sync(entity, domain);
        return entity;
    }

    void sync(ExerciseJpa entity, Exercise domain) {
        entity.exerciseType = domain.getType();
        entity.topic = domain.getTopic();
        entity.thematicTopic = domain.getThematicTopic();
        entity.status = domain.getStatus();
        entity.content = domain.getContent();
        entity.expectedAnswers = writeJson(domain.getExpectedAnswers());
        entity.items = writeJson(domain.getItems());
        entity.getWordInfoIds().clear();
        domain.getWordInfoIds().forEach(id -> entity.getWordInfoIds().add(id.value()));
    }

    Exercise toDomain(ExerciseJpa entity) {
        var exercise = Exercise.create(
                new ExerciseId(entity.getId()),
                entity.getExerciseType(),
                entity.getTopic(),
                entity.getThematicTopic(),
                entity.getWordInfoIds().stream().map(WordInfoId::new).collect(Collectors.toSet()));

        if (entity.getContent() != null) {
            exercise.setContent(entity.getContent());
        }
        exercise.setExpectedAnswers(readStrings(entity.getExpectedAnswers()));
        exercise.setItems(readItems(entity.getItems()));

        if (entity.getStatus() == ExerciseStatus.ANSWERED) {
            exercise.markAnswered();
        } else if (entity.getStatus() == ExerciseStatus.CHECKED) {
            exercise.markChecked();
        }
        return exercise;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize exercise JSON field", e);
        }
    }

    private List<String> readStrings(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize expectedAnswers JSON", e);
        }
    }

    private List<ExerciseItem> readItems(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize exercise items JSON", e);
        }
    }
}
