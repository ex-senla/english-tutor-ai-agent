package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.shared.api.SnapshotProvider;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.LessonStatus;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
// TODO: remove SnapshotProvider when switching to JPA/PostgreSQL
public class InMemoryLessonRepository implements LessonRepository, SnapshotProvider {

    private final Map<LessonId, Lesson> store = new ConcurrentHashMap<>();

    @Override
    public Lesson save(Lesson lesson) {
        store.put(lesson.getId(), lesson);
        return lesson;
    }

    @Override
    public Optional<Lesson> findById(LessonId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Lesson> findActiveByStudentId(StudentId studentId) {
        return store.values().stream()
            .filter(l -> java.util.Objects.equals(l.getStudentId(), studentId))
            .filter(l -> l.getStatus() == LessonStatus.ACTIVE)
            .findFirst();
    }

    // TODO: remove when switching to JPA/PostgreSQL
    public Map<LessonId, Lesson> snapshot() {
        return Map.copyOf(store);
    }
}
