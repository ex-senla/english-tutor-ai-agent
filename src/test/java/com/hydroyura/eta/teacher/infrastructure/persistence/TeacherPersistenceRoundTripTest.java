package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherPersistenceRoundTripTest extends PostgresIntegrationTestBase {

    @Autowired
    private TeacherRepository repository;

    @Test
    void shouldRoundTripTeacherWithIdentifiersAndStudents() {
        var teacher = Teacher.create(TeacherId.generate(), "John");
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, 123456L);
        var studentId = StudentId.generate();
        teacher.addStudent(studentId);
        repository.save(teacher);

        var loaded = repository.findById(teacher.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("John");
        Long telegram = loaded.getIdentifiers().get(IdentifierType.TELEGRAM);
        assertThat(telegram).isEqualTo(123456L);
        assertThat(loaded.getStudentIds()).containsExactly(studentId);
    }

    @Test
    void shouldFindTeacherByIdentifier() {
        var teacher = Teacher.create(TeacherId.generate(), "John");
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, 789012L);
        teacher.addStudent(StudentId.generate());
        repository.save(teacher);

        var found = repository.findByIdentifier(IdentifierType.TELEGRAM, 789012L).orElseThrow();
        assertThat(found.getId()).isEqualTo(teacher.getId());
        Long telegram = found.getIdentifiers().get(IdentifierType.TELEGRAM);
        assertThat(telegram).isEqualTo(789012L);
        assertThat(found.getStudentIds()).hasSize(1);

        assertThat(repository.findByIdentifier(IdentifierType.TELEGRAM, 999L)).isEmpty();
    }
}
