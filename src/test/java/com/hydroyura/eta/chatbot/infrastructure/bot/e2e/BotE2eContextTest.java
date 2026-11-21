package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

class BotE2eContextTest extends BotE2eTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextStartsWithRealPipeline() {
        assertThat(bot).isNotNull();
        assertThat(Mockito.mockingDetails(bot).isMock()).isFalse();

        // telegrambots.enabled=false исключает стартерные бины: ни clearWebhook, ни long-polling
        assertThat(applicationContext.getBeanProvider(TelegramBotsApi.class).getIfAvailable()).isNull();
        assertThat(applicationContext.getBeanProvider(org.telegram.telegrambots.starter.TelegramBotInitializer.class)
                .getIfAvailable()).isNull();

        // app-овый BotInitializer замокан -> @PostConstruct (регистрация бота и SetMyCommands) не выполнялся
        verifyNoInteractions(botInitializer);
    }
}
