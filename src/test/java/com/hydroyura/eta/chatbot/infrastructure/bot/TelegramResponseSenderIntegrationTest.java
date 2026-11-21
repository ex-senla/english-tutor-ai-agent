package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.bots.DefaultAbsSender;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramResponseSenderIntegrationTest extends PostgresIntegrationTestBase {

    @DynamicPropertySource
    static void botProperties(DynamicPropertyRegistry registry) {
        registry.add("telegram.bot.enabled", () -> "true");
        registry.add("telegram.bot.username", () -> "test-bot");
        registry.add("telegrambots.enabled", () -> "false");
    }

    @MockitoBean
    BotInitializer botInitializer;

    @Autowired
    TelegramResponseSender telegramResponseSender;

    @Autowired
    ObjectProvider<DefaultAbsSender> defaultAbsSenderProvider;

    @Test
    void shouldResolveRealBotNotCglibProxy() {
        var resolved = defaultAbsSenderProvider.getObject();

        assertThat(resolved).isNotNull();
        assertThat(resolved).isInstanceOf(EnglishTutorBot.class);
        assertThat(AopUtils.isAopProxy(resolved)).isFalse();
        assertThat(readField(resolved, "objectMapper")).isNotNull();
        assertThat(telegramResponseSender).isNotNull();
    }

    private Object readField(Object target, String name) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot read field " + name, e);
            }
        }
        throw new IllegalStateException("Field not found: " + name);
    }
}
