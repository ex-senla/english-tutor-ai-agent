package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.chatbot.infrastructure.bot.BotInitializer;
import com.hydroyura.eta.chatbot.infrastructure.bot.EnglishTutorBot;
import com.hydroyura.eta.chatbot.infrastructure.bot.ResponseSender;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * База in-process E2E-тестов бота: реальный {@code EnglishTutorBot} и его коллабораторы на
 * реальном Testcontainers PostgreSQL, исходящие вызовы перехвачены моком {@link ResponseSender},
 * LLM-порты и app-овый {@code BotInitializer} замоканы.
 */
public abstract class BotE2eTestBase extends PostgresIntegrationTestBase {

    protected static final Long CHAT_ID = 777L;

    @DynamicPropertySource
    static void botProperties(DynamicPropertyRegistry registry) {
        // app-бины бота (EnglishTutorBot, TelegramResponseSender, BotInitializer)
        registry.add("telegram.bot.enabled", () -> "true");
        // отключить автоконфигурацию telegrambots-spring-boot-starter (TelegramBotsApi + стартер)
        registry.add("telegrambots.enabled", () -> "false");
    }

    @MockitoBean
    protected BotInitializer botInitializer;

    @MockitoBean
    protected ResponseSender responseSender;

    @MockitoBean
    protected WordTranslator wordTranslator;

    @MockitoBean
    protected ExerciseGenerator exerciseGenerator;

    @MockitoBean
    protected CustomTopicValidator customTopicValidator;

    @Autowired
    protected EnglishTutorBot bot;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected BotTester botTester;

    protected OutgoingCallsRecorder outgoingCallsRecorder;

    @BeforeEach
    void cleanDatabase() throws Exception {
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
        jdbcTemplate.execute("DELETE FROM support_message");
        jdbcTemplate.execute("DELETE FROM support_thread");
        jdbcTemplate.execute("DELETE FROM chat");

        outgoingCallsRecorder = new OutgoingCallsRecorder();
        outgoingCallsRecorder.install(responseSender);
        botTester = tester(CHAT_ID);
    }

    protected BotTester tester(Long chatId) {
        return new BotTester(bot, responseSender, new BotDbInspector(jdbcTemplate), chatId, outgoingCallsRecorder);
    }
}
