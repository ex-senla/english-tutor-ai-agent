package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.SearchStudentsQuery;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        return studentRepository.findAllActiveByIds(query.studentIds()).stream()
                .filter(s -> s.getName().equalsIgnoreCase(query.name()))
                .map(Student::getId)
                .findFirst();
    }

    @Override
    public Optional<DictionaryId> getDictionaryId(StudentId studentId) {
        return studentRepository.findById(studentId)
                .filter(s -> !s.isDeleted())
                .map(s -> s.getDictionaryId());
    }

    @Override
    public List<StudentInfo> findStudentsByIds(Set<StudentId> ids) {
        var byId = studentRepository.findAllActiveByIds(ids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(s -> new StudentInfo(s.getId(), s.getName()))
                .toList();
    }

    @Override
    public List<StudentInfo> searchByNameFragment(SearchStudentsQuery query) {
        var fragment = query.fragment().toLowerCase();
        return studentRepository.findAllActiveByIds(query.studentIds()).stream()
                .filter(s -> s.getName().toLowerCase().contains(fragment))
                .map(s -> new StudentInfo(s.getId(), s.getName()))
                .sorted(Comparator.comparing(StudentInfo::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public Optional<StudentDetails> findStudentDetails(StudentId studentId) {
        return studentRepository.findById(studentId)
                .filter(s -> !s.isDeleted())
                .map(student -> {
                    var stats = findWords.getStats(student.getDictionaryId());
                    var hasActiveLesson = findActiveLesson.findByStudentId(studentId).isPresent();
                    return new StudentDetails(student.getName(), stats, hasActiveLesson, student.getLevel());
                });
    }

    @Override
    public Optional<CefrLevel> getCefrLevel(StudentId studentId) {
        return studentRepository.findById(studentId)
                .filter(s -> !s.isDeleted())
                .map(Student::getLevel);
    }
}
