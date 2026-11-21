package com.hydroyura.eta.support.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SupportDataRepository extends JpaRepository<SupportThreadJpa, UUID> {

    @Query("""
            SELECT DISTINCT t FROM SupportThreadJpa t
            LEFT JOIN FETCH t.messages
            WHERE t.id = :id
            """)
    Optional<SupportThreadJpa> findWithMessagesById(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT t FROM SupportThreadJpa t
            LEFT JOIN FETCH t.messages
            WHERE t.teacherId = :teacherId
            """)
    List<SupportThreadJpa> findByTeacherIdWithMessages(@Param("teacherId") UUID teacherId);

    @Query("""
            SELECT t FROM SupportThreadJpa t
            LEFT JOIN FETCH t.messages
            ORDER BY COALESCE(t.lastMessageAt, t.createdAt) DESC
            """)
    List<SupportThreadJpa> findAllWithMessages();
}
