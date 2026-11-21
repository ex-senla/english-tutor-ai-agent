package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Чтение персистентного состояния в обход JPA-кэшей (JDBC) — доказательство того, что
 * чат и данные реально попали в PostgreSQL.
 */
public final class BotDbInspector {

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BotDbInspector(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String chatState(Long chatId) {
        return jdbcTemplate.queryForObject("SELECT state FROM chat WHERE chat_id = ?", String.class, chatId);
    }

    public Map<String, Object> chatContext(Long chatId) {
        var json = jdbcTemplate.queryForObject("SELECT context::text FROM chat WHERE chat_id = ?", String.class,
                chatId);
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse chat context JSON", e);
        }
    }

    public List<Map<String, Object>> rows(String table) {
        return jdbcTemplate.queryForList("SELECT * FROM " + table);
    }

    public int count(String table) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }
}
