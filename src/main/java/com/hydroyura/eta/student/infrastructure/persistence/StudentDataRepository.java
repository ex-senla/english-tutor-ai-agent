package com.hydroyura.eta.student.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface StudentDataRepository extends JpaRepository<StudentJpa, UUID> {

    List<StudentJpa> findByIdInAndDeletedFalse(Collection<UUID> ids);

    boolean existsByIdInAndDeletedFalseAndNameIgnoreCase(Collection<UUID> ids, String name);
}
