package com.hydroyura.eta.chatbot.infrastructure.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
public class BotInitializer {

    private final EnglishTutorBot bot;

    public BotInitializer(EnglishTutorBot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void init() throws Exception {
        new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
        registerCommandMenu();
    }

    private void registerCommandMenu() {
        try {
            bot.execute(SetMyCommands.builder().commands(BotCommands.list()).build());
        } catch (Exception e) {
            log.warn("Failed to register bot command menu: {}", e.getMessage());
        }
    }
}
