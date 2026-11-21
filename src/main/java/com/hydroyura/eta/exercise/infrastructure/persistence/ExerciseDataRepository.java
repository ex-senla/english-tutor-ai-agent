package com.hydroyura.eta.exercise.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ExerciseDataRepository extends JpaRepository<ExerciseJpa, UUID> {

    @Query("""
            SELECT e FROM ExerciseJpa e
            LEFT JOIN FETCH e.wordInfoIds
            WHERE e.id = :id
            """)
    Optional<ExerciseJpa> findWithWordInfoIds(@Param("id") UUID id);
}
