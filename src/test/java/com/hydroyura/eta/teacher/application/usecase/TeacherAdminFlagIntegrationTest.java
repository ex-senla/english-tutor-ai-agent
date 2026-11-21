package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherAdminFlagIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private RegisterTeacher registerTeacher;

    @Autowired
    private CreateStudentWithDictionary createStudentWithDictionary;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void adminFlagShouldSurviveDomainSave() {
        var teacherId = registerTeacher.execute(new RegisterTeacherCommand(11_001L, "Admin"));
        jdbcTemplate.update("UPDATE teacher SET admin = TRUE WHERE id = ?", teacherId.value());

        createStudentWithDictionary.execute(new CreateStudentWithDictionaryCommand(
                teacherId, "Иван", "Словарь Ивана", CefrLevel.B1));

        assertThat(adminFlag(teacherId)).isTrue();
    }

    @Test
    void newTeacherShouldHaveAdminFalse() {
        var teacherId = registerTeacher.execute(new RegisterTeacherCommand(11_002L, "Teacher"));

        assertThat(adminFlag(teacherId)).isFalse();
    }

    private Boolean adminFlag(TeacherId teacherId) {
        return jdbcTemplate.queryForObject("SELECT admin FROM teacher WHERE id = ?", Boolean.class, teacherId.value());
    }
}
