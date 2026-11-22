package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOnboardingUseCaseTest {

    private final TeacherRepository repository = mock(TeacherRepository.class);

    private final CompleteOnboardingUseCase useCase = new CompleteOnboardingUseCase(repository);

    @Test
    void shouldCompleteOnboardingAndSave() {
        var teacherId = TeacherId.generate();
        var teacher = Teacher.create(teacherId, "John");
        when(repository.findById(teacherId)).thenReturn(Optional.of(teacher));

        useCase.execute(teacherId);

        assertThat(teacher.isOnboardingCompleted()).isTrue();
        verify(repository).findById(teacherId);
        verify(repository).save(teacher);
    }

    @Test
    void shouldThrowWhenTeacherMissing() {
        var teacherId = TeacherId.generate();
        when(repository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(teacherId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(teacherId.value().toString());
    }
}
