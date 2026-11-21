package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherDirectory;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherDirectoryQueryCountTest extends PostgresIntegrationTestBase {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherDirectory teacherDirectory;

    @Test
    void shouldUseConstantQueriesForFindAll() {
        seedTeacher(100L, 3);
        seedTeacher(200L, 10);
        seedTeacher(300L, 1);

        clearSession();
        var smallWindow = measureWindow(() -> teacherDirectory.findAll());

        seedTeacher(400L, 20);
        seedTeacher(500L, 2);
        clearSession();
        var largeWindow = measureWindow(() -> teacherDirectory.findAll());

        assertThat(largeWindow.prepare()).isEqualTo(smallWindow.prepare());
        assertThat(largeWindow.queryExecution()).isEqualTo(smallWindow.queryExecution());
        assertThat(smallWindow.prepare()).isLessThanOrEqualTo(2);
        assertThat(smallWindow.lazyFetches()).isZero();
        assertThat(largeWindow.lazyFetches()).isZero();
    }

    @Test
    void shouldUseSingleQueryForFindTelegramChatId() {
        var teacher = seedTeacher(700L, 2);

        clearSession();
        var window = measureWindow(() -> teacherDirectory.findTelegramChatId(teacher.getId()));

        assertThat(window.prepare()).isLessThanOrEqualTo(2);
        assertThat(window.lazyFetches()).isZero();
    }

    @Test
    void shouldUseScalarQueryForGetStudentIds() {
        var teacher = seedTeacher(800L, 5);

        clearSession();
        var window = measureWindow(() -> teacherDirectory.getStudentIds(teacher.getId()));

        assertThat(window.prepare()).isLessThanOrEqualTo(2);
        assertThat(window.lazyFetches()).isZero();
    }

    private Teacher seedTeacher(Long chatId, int studentCount) {
        var teacher = Teacher.create(TeacherId.generate(), "Teacher" + chatId);
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, chatId);
        for (int i = 0; i < studentCount; i++) {
            teacher.addStudent(StudentId.generate());
        }
        teacherRepository.save(teacher);
        return teacher;
    }
}
