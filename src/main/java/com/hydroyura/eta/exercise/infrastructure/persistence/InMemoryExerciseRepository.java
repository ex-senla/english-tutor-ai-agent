package com.hydroyura.eta.exercise.infrastructure.persistence;

import com.hydroyura.eta.shared.api.SnapshotProvider;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
// TODO: remove SnapshotProvider when switching to JPA/PostgreSQL
public class InMemoryExerciseRepository implements ExerciseRepository, SnapshotProvider {

    private final Map<ExerciseId, Exercise> store = new ConcurrentHashMap<>();

    @Override
    public Exercise save(Exercise exercise) {
        store.put(exercise.getId(), exercise);
        return exercise;
    }

    @Override
    public Optional<Exercise> findById(ExerciseId id) {
        return Optional.ofNullable(store.get(id));
    }

    // TODO: remove when switching to JPA/PostgreSQL
    public Map<ExerciseId, Exercise> snapshot() {
        return Map.copyOf(store);
    }
}
