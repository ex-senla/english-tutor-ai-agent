package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudentQueryService implements StudentQuery {

    private final StudentRepository studentRepository;
    private final FindWords findWords;
    private final FindActiveLesson findActiveLesson;

    @Override
    public boolean existsByName(StudentExistsByNameQuery query) {
        return studentRepository.existsByNameInIds(query.studentIds(), query.name());
    }

    @Override
    public Optional<StudentId> findByNameIn(FindStudentByNameQuery query) {
        return query.studentIds().stream()
            .map(studentRepository::findById)
            .flatMap(Optional::stream)
            .filter(s -> s.getName().equalsIgnoreCase(query.name()))
            .map(s -> s.getId())
            .findFirst();
    }

    @Override
    public Optional<DictionaryId> getDictionaryId(StudentId studentId) {
        return studentRepository.findById(studentId)
            .map(s -> s.getDictionaryId());
    }

    @Override
    public List<StudentInfo> findStudentsByIds(Set<StudentId> ids) {
        return ids.stream()
            .map(studentRepository::findById)
            .flatMap(Optional::stream)
            .map(s -> new StudentInfo(s.getId(), s.getName()))
            .toList();
    }

    @Override
    public Optional<StudentDetails> findStudentDetails(StudentId studentId) {
        return studentRepository.findById(studentId)
            .map(student -> {
                var stats = findWords.getStats(student.getDictionaryId());
                var hasActiveLesson = findActiveLesson.findByStudentId(studentId).isPresent();
                return new StudentDetails(student.getName(), stats, hasActiveLesson);
            });
    }
}
