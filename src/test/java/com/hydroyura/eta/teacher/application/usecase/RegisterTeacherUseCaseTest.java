package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.application.demo.DemoStudentsSeeder;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RegisterTeacherUseCaseTest {

    private RegisterTeacherUseCase useCase;

    private StubTeacherRepository repository;

    private DemoStudentsSeeder seeder;

    @BeforeEach
    void setUp() {
        repository = new StubTeacherRepository();
        seeder = mock(DemoStudentsSeeder.class);
        useCase = new RegisterTeacherUseCase(repository, seeder);
    }

    @Test
    void shouldRegisterTeacher() {
        var id = useCase.execute(new RegisterTeacherCommand(123L, "John"));

        assertThat(id).isNotNull();
        var saved = repository.findById(id).orElseThrow();
        assertThat(saved.getName()).isEqualTo("John");
        assertThat((Long) saved.getIdentifiers().get(IdentifierType.TELEGRAM)).isEqualTo(123L);
    }

    @Test
    void shouldSeedDemoStudentsAfterRegistration() {
        var id = useCase.execute(new RegisterTeacherCommand(123L, "John"));

        verify(seeder).seed(id);
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> useCase.execute(new RegisterTeacherCommand(123L, "  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    // --- stub ---

    static class StubTeacherRepository implements TeacherRepository {

        private final Map<TeacherId, Teacher> store = new HashMap<>();

        @Override
        public Teacher save(Teacher t) {
            store.put(t.getId(), t);
            return t;
        }

        @Override
        public Optional<Teacher> findById(TeacherId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Teacher> findByIdentifier(IdentifierType type, Object value) {
            return store.values().stream().filter(t -> java.util.Objects.equals(value, t.getIdentifiers().get(type)))
                    .findFirst();
        }
    }
}
