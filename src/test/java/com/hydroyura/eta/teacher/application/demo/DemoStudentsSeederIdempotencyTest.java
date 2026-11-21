package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class DemoStudentsSeederIdempotencyTest extends PostgresIntegrationTestBase {

    @DynamicPropertySource
    static void enableDemoSeeding(DynamicPropertyRegistry registry) {
        registry.add("eta.demo-students.enabled", () -> "true");
    }

    @Autowired
    private RegisterTeacher registerTeacher;

    @Autowired
    private DemoStudentsSeeder seeder;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private FindWords findWords;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void shouldRegisterAndSeedIdempotently() {
        var chatId = 555L;

        // (a) регистрация через use case: commit без UnexpectedRollbackException
        registerTeacher.execute(new RegisterTeacherCommand(chatId, "John"));

        var teacher = teacherRepository.findByIdentifier(IdentifierType.TELEGRAM, chatId).orElseThrow();
        assertThat(teacher.getStudentIds()).hasSize(3);
        for (var studentId : teacher.getStudentIds()) {
            var dictionaryId = studentQuery.getDictionaryId(studentId).orElseThrow();
            assertThat(findWords.findByDictionaryId(dictionaryId)).isNotEmpty();
        }

        // (b) повторный запуск сидера в явной транзакции: commit успешен, дублей нет
        transactionTemplate.executeWithoutResult(status -> seeder.seed(teacher.getId()));

        var after = teacherRepository.findByIdentifier(IdentifierType.TELEGRAM, chatId).orElseThrow();
        assertThat(after.getStudentIds()).hasSize(3);
    }
}
