package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.ChangeStudentLevelCommand;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeStudentLevelUseCaseTest {

    private ChangeStudentLevelUseCase useCase;

    private StubStudentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubStudentRepository();
        useCase = new ChangeStudentLevelUseCase(repository);
    }

    @Test
    void shouldChangeLevel() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.A2);
        repository.save(student);

        useCase.execute(new ChangeStudentLevelCommand(student.getId(), CefrLevel.B2));

        assertThat(repository.findById(student.getId()).orElseThrow().getLevel()).isEqualTo(CefrLevel.B2);
    }

    @Test
    void shouldThrowWhenStudentNotFound() {
        assertThatThrownBy(() -> useCase.execute(new ChangeStudentLevelCommand(StudentId.generate(), CefrLevel.A2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Student not found");
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
