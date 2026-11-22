package com.hydroyura.eta.teacher.domain.teacher;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherTest {

    @Test
    void shouldCreateTeacher() {
        var teacher = Teacher.create(TeacherId.generate(), "John");
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, 123L);

        assertThat(teacher.getId()).isNotNull();
        assertThat(teacher.getName()).isEqualTo("John");
        assertThat((Long) teacher.getIdentifiers().get(IdentifierType.TELEGRAM)).isEqualTo(123L);
    }

    @Test
    void shouldCreateTeacherWithOnboardingCompletedFalse() {
        var teacher = Teacher.create(TeacherId.generate(), "John");

        assertThat(teacher.isOnboardingCompleted()).isFalse();
    }

    @Test
    void shouldCompleteOnboarding() {
        var teacher = Teacher.create(TeacherId.generate(), "John");

        teacher.completeOnboarding();

        assertThat(teacher.isOnboardingCompleted()).isTrue();
    }

    @Test
    void shouldCompleteOnboardingIdempotently() {
        var teacher = Teacher.create(TeacherId.generate(), "John");

        teacher.completeOnboarding();
        teacher.completeOnboarding();

        assertThat(teacher.isOnboardingCompleted()).isTrue();
    }

    @Test
    void shouldReconstituteOnboardingFlagBothValues() {
        var id = TeacherId.generate();

        var notCompleted = Teacher.reconstitute(id, "John", false, false);
        var completed = Teacher.reconstitute(id, "John", true, true);

        assertThat(notCompleted.isAdmin()).isFalse();
        assertThat(notCompleted.isOnboardingCompleted()).isFalse();
        assertThat(completed.isAdmin()).isTrue();
        assertThat(completed.isOnboardingCompleted()).isTrue();
    }
}
