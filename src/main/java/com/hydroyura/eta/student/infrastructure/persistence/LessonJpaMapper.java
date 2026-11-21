package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Lesson;
import java.util.stream.Collectors;

final class LessonJpaMapper {

    LessonJpa toEntity(Lesson domain) {
        var entity = new LessonJpa();
        entity.id = domain.getId().value();
        sync(entity, domain);
        return entity;
    }

    void sync(LessonJpa entity, Lesson domain) {
        entity.studentId = domain.getStudentId().value();
        entity.name = domain.getName();
        entity.status = domain.getStatus();
        entity.startedAt = domain.getStartedAt();
        entity.endedAt = domain.getEndedAt();
        entity.getWordInfoIds().clear();
        domain.getWordInfoIds().forEach(id -> entity.getWordInfoIds().add(id.value()));
    }

    Lesson toDomain(LessonJpa entity) {
        var wordInfoIds = entity.getWordInfoIds().stream()
                .map(WordInfoId::new)
                .collect(Collectors.toSet());
        return Lesson.restore(
                new LessonId(entity.getId()),
                new StudentId(entity.getStudentId()),
                entity.getName(),
                wordInfoIds,
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getEndedAt());
    }
}
