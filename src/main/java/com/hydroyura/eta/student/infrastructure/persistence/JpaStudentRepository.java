package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaStudentRepository implements StudentRepository {

    private final StudentDataRepository dataRepository;

    private final StudentJpaMapper mapper = new StudentJpaMapper();

    private final EntityManager entityManager;

    public JpaStudentRepository(StudentDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Student save(Student student) {
        var id = student.getId().value();
        var existing = dataRepository.findById(id);
        if (existing.isEmpty()) {
            entityManager.persist(mapper.toEntity(student));
        } else {
            mapper.sync(existing.get(), student);
        }
        return student;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findById(StudentId id) {
        return dataRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameInIds(Set<StudentId> ids, String name) {
        var rawIds = ids.stream().map(StudentId::value).toList();
        if (rawIds.isEmpty()) {
            return false;
        }
        return dataRepository.existsByIdInAndDeletedFalseAndNameIgnoreCase(rawIds, name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAllActiveByIds(Set<StudentId> ids) {
        var rawIds = ids.stream().map(StudentId::value).toList();
        if (rawIds.isEmpty()) {
            return List.of();
        }
        return dataRepository.findByIdInAndDeletedFalse(rawIds).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
