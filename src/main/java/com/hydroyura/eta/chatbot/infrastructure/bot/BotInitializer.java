package com.hydroyura.eta.chatbot.infrastructure.bot;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final EnglishTutorBot bot;

    public BotInitializer(EnglishTutorBot bot) { this.bot = bot; }

    @PostConstruct
    public void init() throws Exception {
        new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
    }
}
