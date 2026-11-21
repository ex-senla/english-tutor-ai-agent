package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.LessonStatus;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaLessonRepository implements LessonRepository {

    private final LessonDataRepository dataRepository;

    private final LessonJpaMapper mapper = new LessonJpaMapper();

    private final EntityManager entityManager;

    public JpaLessonRepository(LessonDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Lesson save(Lesson lesson) {
        var id = lesson.getId().value();
        var existing = dataRepository.findWithWordInfoIds(id);
        if (existing.isEmpty()) {
            entityManager.persist(mapper.toEntity(lesson));
        } else {
            mapper.sync(existing.get(), lesson);
        }
        return lesson;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lesson> findById(LessonId id) {
        return dataRepository.findWithWordInfoIds(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lesson> findActiveByStudentId(StudentId studentId) {
        return dataRepository.findByStudentIdAndStatusWithWordInfoIds(studentId.value(), LessonStatus.ACTIVE)
                .map(mapper::toDomain);
    }
}
