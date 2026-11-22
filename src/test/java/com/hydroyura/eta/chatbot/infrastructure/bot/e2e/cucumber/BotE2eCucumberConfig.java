package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.BotInitializer;
import com.hydroyura.eta.chatbot.infrastructure.bot.ResponseSender;
import com.hydroyura.eta.dictionary.api.word.WordTranslator;
import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Контекст Cucumber-прогона: зеркалит конфигурацию {@code BotE2eTestBase} без наследования
 * JUnit-специфичной {@code PostgresIntegrationTestBase} (в cucumber-прогоне JUnit-расширения
 * {@code @Testcontainers}/@{@code Container} не запускаются), поэтому контейнер стартует eager
 * в static-инициализаторе, а datasource и флаги бота задаются через {@code @DynamicPropertySource}.
 */
@CucumberContextConfiguration
@SpringBootTest(properties = {
        "telegram.bot.token=test-token",
        "telegram.bot.username=eta_tutor_bot",
        "spring.ai.openai.api-key=test",
        "spring.ai.openai.base-url=http://localhost:8080",
        "spring.ai.openai.chat.model=test-model",
        "eta.demo-students.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
public class BotE2eCucumberConfig {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // реальный EnglishTutorBot и его коллабораторы
        registry.add("telegram.bot.enabled", () -> "true");
        // отключить автоконфигурацию telegrambots-spring-boot-starter (TelegramBotsApi + стартер)
        registry.add("telegrambots.enabled", () -> "false");
    }

    @MockitoBean
    BotInitializer botInitializer;

    @MockitoBean
    ResponseSender responseSender;

    @MockitoBean
    WordTranslator wordTranslator;

    @MockitoBean
    ExerciseGenerator exerciseGenerator;

    @MockitoBean
    CustomTopicValidator customTopicValidator;
}
