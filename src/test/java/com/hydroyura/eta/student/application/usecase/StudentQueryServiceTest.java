package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentQueryServiceTest {

    private StudentQueryService service;

    private StubStudentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubStudentRepository();
        var findWords = mock(FindWords.class);
        var findActiveLesson = mock(FindActiveLesson.class);
        when(findWords.getStats(any())).thenReturn(new DictionaryStats(3, 1, 1, 1));
        when(findActiveLesson.findByStudentId(any())).thenReturn(Optional.empty());
        service = new StudentQueryService(repository, findWords, findActiveLesson);
    }

    @Test
    void shouldReturnCefrLevel() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.B1);
        repository.save(student);

        assertThat(service.getCefrLevel(student.getId())).contains(CefrLevel.B1);
    }

    @Test
    void shouldReturnEmptyCefrLevelWhenStudentMissing() {
        assertThat(service.getCefrLevel(StudentId.generate())).isEmpty();
    }

    @Test
    void shouldIncludeLevelInStudentDetails() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.C1);
        repository.save(student);

        var details = service.findStudentDetails(student.getId()).orElseThrow();

        assertThat(details.name()).isEqualTo("Иван");
        assertThat(details.level()).isEqualTo(CefrLevel.C1);
    }

    static class StubStudentRepository implements StudentRepository {

        private final Map<StudentId, Student> store = new HashMap<>();

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
            return ids.stream().map(store::get).anyMatch(s -> s != null && s.getName().equalsIgnoreCase(name));
        }
    }
}
