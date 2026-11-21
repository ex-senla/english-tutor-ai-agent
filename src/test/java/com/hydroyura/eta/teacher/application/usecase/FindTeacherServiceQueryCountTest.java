package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FindTeacherServiceQueryCountTest extends PostgresIntegrationTestBase {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private FindTeacher findTeacher;

    @Test
    void shouldUseConstantQueriesForGetStudentIds() {
        seedTeacher(100L, 3);
        seedTeacher(200L, 10);

        clearSession();
        var smallWindow = measureWindow(() -> findTeacher.getStudentIds(100L));
        clearSession();
        var largeWindow = measureWindow(() -> findTeacher.getStudentIds(200L));

        assertThat(largeWindow.prepare()).isEqualTo(smallWindow.prepare());
        assertThat(largeWindow.queryExecution()).isEqualTo(smallWindow.queryExecution());
        assertThat(smallWindow.prepare()).isLessThanOrEqualTo(2);
        assertThat(smallWindow.lazyFetches()).isZero();
        assertThat(largeWindow.lazyFetches()).isZero();
    }

    private void seedTeacher(Long chatId, int studentCount) {
        var teacher = Teacher.create(TeacherId.generate(), "Teacher" + chatId);
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, chatId);
        for (int i = 0; i < studentCount; i++) {
            teacher.addStudent(StudentId.generate());
        }
        teacherRepository.save(teacher);
    }
}
