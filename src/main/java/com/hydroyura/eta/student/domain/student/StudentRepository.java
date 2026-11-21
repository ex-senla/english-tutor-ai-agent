package com.hydroyura.eta.student.domain.student;

import com.hydroyura.eta.student.api.student.StudentId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StudentRepository {

    Student save(Student student);

    Optional<Student> findById(StudentId id);

    boolean existsByNameInIds(Set<StudentId> ids, String name);

    List<Student> findAllActiveByIds(Set<StudentId> ids);
}
