package com.hydroyura.eta.exercise.infrastructure.persistence;

import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaExerciseRepository implements ExerciseRepository {

    private final ExerciseDataRepository dataRepository;

    private final ExerciseJpaMapper mapper = new ExerciseJpaMapper();

    private final EntityManager entityManager;

    public JpaExerciseRepository(ExerciseDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Exercise save(Exercise exercise) {
        var id = exercise.getId().value();
        var existing = dataRepository.findWithWordInfoIds(id);
        if (existing.isEmpty()) {
            entityManager.persist(mapper.toEntity(exercise));
        } else {
            mapper.sync(existing.get(), exercise);
        }
        return exercise;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Exercise> findById(ExerciseId id) {
        return dataRepository.findWithWordInfoIds(id.value()).map(mapper::toDomain);
    }
}
