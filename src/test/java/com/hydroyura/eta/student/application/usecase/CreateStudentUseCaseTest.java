package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.CreateStudentCommand;
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

class CreateStudentUseCaseTest {

    private CreateStudentUseCase useCase;

    private StubStudentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubStudentRepository();
        useCase = new CreateStudentUseCase(repository);
    }

    @Test
    void shouldCreateStudent() {
        var cmd = new CreateStudentCommand("Иван", DictionaryId.generate());
        var studentId = useCase.execute(cmd);

        assertThat(studentId).isNotNull();
        var saved = repository.findById(studentId).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Иван");
        assertThat(saved.getDictionaryId()).isEqualTo(cmd.dictionaryId());
    }

    @Test
    void shouldRejectBlankName() {
        var cmd = new CreateStudentCommand("  ", DictionaryId.generate());

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldReturnStudentId() {
        var cmd = new CreateStudentCommand("Иван", DictionaryId.generate());
        var result = useCase.execute(cmd);

        assertThat(result).isInstanceOf(StudentId.class);
    }

    // --- stub ---

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
