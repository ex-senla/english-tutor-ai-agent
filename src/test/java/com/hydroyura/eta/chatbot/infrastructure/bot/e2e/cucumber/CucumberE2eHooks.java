package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.BotInitializer;
import com.hydroyura.eta.chatbot.infrastructure.bot.EnglishTutorBot;
import com.hydroyura.eta.chatbot.infrastructure.bot.ResponseSender;
import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.BotDbInspector;
import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.BotTester;
import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.E2eTestTables;
import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.OutgoingCallsRecorder;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import io.cucumber.java.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber-хук {@code @Before}: сбрасывает состояние моков, очищает БД по общему перечню
 * {@link E2eTestTables} и собирает свежий {@link BotTester} для сценария. Дополнительно
 * детерминированно доказывает, что Spring-контекст не пересоздаётся между сценариями.
 */
public class CucumberE2eHooks {

    private static ApplicationContext firstContext;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BotInitializer botInitializer;

    @Autowired
    private ResponseSender responseSender;

    @Autowired
    private WordTranslator wordTranslator;

    @Autowired
    private ExerciseGenerator exerciseGenerator;

    @Autowired
    private CustomTopicValidator customTopicValidator;

    @Autowired
    private EnglishTutorBot bot;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CucumberScenarioState state;

    @Before
    public void resetScenario() throws TelegramApiException {
        assertSingleApplicationContext();

        Mockito.reset(botInitializer, responseSender, wordTranslator, exerciseGenerator, customTopicValidator);

        for (String table : E2eTestTables.ALL) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }

        var recorder = new OutgoingCallsRecorder();
        recorder.install(responseSender);
        state.setChatId(CucumberScenarioState.CHAT_ID);
        state.setStudentId(null);
        state.setStudentName(null);
        state.setStudentLevel(null);
        state.setWordInfoId(null);
        state.setBotTester(new BotTester(bot, responseSender, new BotDbInspector(jdbcTemplate),
                CucumberScenarioState.CHAT_ID, recorder));
    }

    private void assertSingleApplicationContext() {
        if (firstContext == null) {
            firstContext = applicationContext;
        } else {
            assertThat(applicationContext).isSameAs(firstContext);
        }
    }
}
