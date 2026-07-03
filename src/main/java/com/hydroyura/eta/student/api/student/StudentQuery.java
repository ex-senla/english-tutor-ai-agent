package com.hydroyura.eta.student.api.student;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StudentQuery {

    boolean existsByName(StudentExistsByNameQuery query);

    Optional<StudentId> findByNameIn(FindStudentByNameQuery query);

    Optional<DictionaryId> getDictionaryId(StudentId studentId);

    List<StudentInfo> findStudentsByIds(Set<StudentId> ids);

    Optional<StudentDetails> findStudentDetails(StudentId studentId);
}
