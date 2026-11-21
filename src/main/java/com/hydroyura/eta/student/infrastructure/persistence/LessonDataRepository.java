package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.student.domain.student.LessonStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LessonDataRepository extends JpaRepository<LessonJpa, UUID> {

    @Query("""
            SELECT l FROM LessonJpa l
            LEFT JOIN FETCH l.wordInfoIds
            WHERE l.id = :id
            """)
    Optional<LessonJpa> findWithWordInfoIds(@Param("id") UUID id);

    @Query("""
            SELECT l FROM LessonJpa l
            LEFT JOIN FETCH l.wordInfoIds
            WHERE l.studentId = :studentId AND l.status = :status
            """)
    Optional<LessonJpa> findByStudentIdAndStatusWithWordInfoIds(
            @Param("studentId") UUID studentId,
            @Param("status") LessonStatus status);
}
