package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Query("""
            SELECT new com.hydroyura.eta.teacher.infrastructure.persistence.TeacherRow(
                t.id, t.name, t.admin)
            FROM TeacherJpa t
            ORDER BY t.name
            """)
    List<TeacherRow> findAllInfos();

    @Query("""
            SELECT i.identifierValue FROM TeacherIdentifierJpa i
            WHERE i.id.teacherId = :teacherId AND i.id.identifierType = :type
            """)
    Optional<String> findIdentifierValue(
            @Param("teacherId") UUID teacherId,
            @Param("type") IdentifierType type);

    @Query(value = """
            SELECT ts.student_id FROM teacher_student ts WHERE ts.teacher_id = :teacherId
            """, nativeQuery = true)
    Set<UUID> findStudentIds(@Param("teacherId") UUID teacherId);
}
