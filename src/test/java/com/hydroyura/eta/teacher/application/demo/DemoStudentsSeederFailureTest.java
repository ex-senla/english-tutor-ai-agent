package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DemoStudentsSeederFailureTest extends PostgresIntegrationTestBase {

    @DynamicPropertySource
    static void demoSeedingProperties(DynamicPropertyRegistry registry) {
        registry.add("eta.demo-students.enabled", () -> "true");
        registry.add("eta.demo-students.student-count", () -> "3");
        registry.add("eta.demo-students.min-words-per-student", () -> "1");
        registry.add("eta.demo-students.max-words-per-student", () -> "1");
    }

    @Autowired
    private RegisterTeacher registerTeacher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private AddWordToDictionary addWordToDictionary;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM exercise_word_info");
        jdbcTemplate.execute("DELETE FROM word_info_translation");
        jdbcTemplate.execute("DELETE FROM word_info");
        jdbcTemplate.execute("DELETE FROM lesson_word_info");
        jdbcTemplate.execute("DELETE FROM lesson");
        jdbcTemplate.execute("DELETE FROM student");
        jdbcTemplate.execute("DELETE FROM teacher_identifier");
        jdbcTemplate.execute("DELETE FROM teacher_student");
        jdbcTemplate.execute("DELETE FROM teacher");
        jdbcTemplate.execute("DELETE FROM dictionary");
        jdbcTemplate.execute("DELETE FROM exercise");
        jdbcTemplate.execute("DELETE FROM chat");
    }

    @Test
    void shouldRollbackRegistrationWhenSeedingFails() {
        when(addWordToDictionary.execute(any()))
                .thenReturn(WordInfoId.generate())
                .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> registerTeacher.execute(new RegisterTeacherCommand(666L, "John")))
                .isInstanceOf(RuntimeException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM teacher", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM student", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dictionary", Integer.class)).isZero();
    }
}
