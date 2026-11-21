package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TeacherDataRepository extends JpaRepository<TeacherJpa, UUID> {

    @Query("""
            SELECT t FROM TeacherJpa t
            LEFT JOIN FETCH t.identifiers
            LEFT JOIN FETCH t.studentIds
            WHERE t.id = :id
            """)
    Optional<TeacherJpa> findWithChildren(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT t FROM TeacherJpa t
            JOIN FETCH t.identifiers i
            LEFT JOIN FETCH t.studentIds
            WHERE i.id.identifierType = :type AND i.identifierValue = :value
            """)
    Optional<TeacherJpa> findByIdentifier(
            @Param("type") IdentifierType type,
            @Param("value") String value);
}
