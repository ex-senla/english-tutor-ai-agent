package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.shared.api.SnapshotProvider;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
// TODO: remove SnapshotProvider when switching to JPA/PostgreSQL
public class InMemoryStudentRepository implements StudentRepository, SnapshotProvider {

    private final Map<StudentId, Student> store = new ConcurrentHashMap<>();

    @Override
    public Student save(Student student) {
        store.put(student.getId(), student);
        return student;
    }

    @Override
    public Optional<Student> findById(StudentId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsByNameInIds(Set<StudentId> ids, String name) {
        return ids.stream()
            .map(store::get)
            .filter(java.util.Objects::nonNull)
            .anyMatch(s -> s.getName().equalsIgnoreCase(name));
    }

    // TODO: remove when switching to JPA/PostgreSQL
    public Map<StudentId, Student> snapshot() {
        return Map.copyOf(store);
    }
}
