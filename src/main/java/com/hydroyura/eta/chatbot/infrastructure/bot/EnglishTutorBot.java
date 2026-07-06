package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.StateMachineAppService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class EnglishTutorBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final StateMachineAppService service;
    private final UpdateParser updateParser;
    private final SendMessageConverter converter;

    public EnglishTutorBot(@Value("${telegram.bot.token}") String botToken,
                           @Value("${telegram.bot.username}") String botUsername,
                           StateMachineAppService service, UpdateParser updateParser, SendMessageConverter converter) {
        super(botToken);
        this.botUsername = botUsername;
        this.service = service;
        this.updateParser = updateParser;
        this.converter = converter;
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    @SneakyThrows // TODO: create exception handler
    public void onUpdateReceived(Update update) {
        // 0. get chatId
        var chatId = update.getMessage().getChatId();
        // 1. get stateMachine
        var sm = service.getOrCreate(chatId);

        // 2. parse update to select action
        var action = updateParser.parseUpdate(update);

        // 3. perform action in sm
        var result = sm.performAction(action);

        // 4. save sm
        service.save(sm);

        // 5. prepare response
        var sendMessage = converter.convert(result, chatId);

        // 6. send response message
        execute(sendMessage);
    }


}
