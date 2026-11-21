package com.hydroyura.eta.dictionary.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DictionaryDataRepository extends JpaRepository<DictionaryJpa, UUID> {

    @Query("""
            SELECT d FROM DictionaryJpa d
            LEFT JOIN FETCH d.wordInfos w
            LEFT JOIN FETCH w.translations
            WHERE d.id = :id
            """)
    Optional<DictionaryJpa> findWithWordInfos(@Param("id") UUID id);
}
