package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherJpaMapperTest {

    private final TeacherJpaMapper mapper = new TeacherJpaMapper();

    @Test
    void syncShouldNotTouchAdminFlag() {
        var teacher = Teacher.reconstitute(TeacherId.generate(), "John", false, false);
        teacher.addStudent(StudentId.generate());

        var entity = new TeacherJpa();
        entity.id = teacher.getId().value();
        entity.admin = true;

        mapper.sync(entity, teacher);

        assertThat(entity.isAdmin()).isTrue();
    }

    @Test
    void toDomainShouldReadAdminFlag() {
        var entity = new TeacherJpa();
        entity.id = TeacherId.generate().value();
        entity.name = "John";
        entity.admin = true;
        entity.getStudentIds().add(StudentId.generate().value());

        var teacher = mapper.toDomain(entity);

        assertThat(teacher.isAdmin()).isTrue();
        assertThat(teacher.getStudentIds()).hasSize(1);
        assertThat((Long) teacher.getIdentifiers().get(IdentifierType.TELEGRAM)).isNull();
    }

    @Test
    void syncShouldWriteOnboardingCompletedFromDomain() {
        var teacher = Teacher.create(TeacherId.generate(), "John");
        teacher.completeOnboarding();

        var entity = new TeacherJpa();
        entity.id = teacher.getId().value();
        entity.onboardingCompleted = false;

        mapper.sync(entity, teacher);

        assertThat(entity.isOnboardingCompleted()).isTrue();
    }

    @Test
    void toDomainShouldRoundTripOnboardingCompletedBothValues() {
        assertThat(mapper.toDomain(entity(false)).isOnboardingCompleted()).isFalse();
        assertThat(mapper.toDomain(entity(true)).isOnboardingCompleted()).isTrue();
    }

    private TeacherJpa entity(boolean onboardingCompleted) {
        var entity = new TeacherJpa();
        entity.id = TeacherId.generate().value();
        entity.name = "John";
        entity.onboardingCompleted = onboardingCompleted;
        return entity;
    }
}
