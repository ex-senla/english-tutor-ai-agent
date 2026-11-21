package com.hydroyura.eta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke-тест контекста приложения на реальном PostgreSQL (Testcontainers):
 * контекст собирается без Telegram/AI credentials, Flyway применяет миграции.
 */
class EtaApplicationTests extends PostgresIntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndFlywayApplied() {
        var count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(1);

        var tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name",
                String.class);
        assertThat(tables).contains(
                "dictionary", "word_info", "word_info_translation", "student", "lesson", "lesson_word_info",
                "teacher", "teacher_identifier", "teacher_student", "exercise", "exercise_word_info", "chat");
    }
}
