package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.StateMachineAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Component
public class EnglishTutorBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final StateMachineAppService service;

    public EnglishTutorBot(@Value("${telegram.bot.token}") String botToken,
                           @Value("${telegram.bot.username}") String botUsername,
                           StateMachineAppService service) {
        super(botToken);
        this.botUsername = botUsername;
        this.service = service;
    }

    @Override public String getBotUsername() { return botUsername; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        var msg = update.getMessage();
        var chatId = msg.getChatId();
        var text = msg.getText().trim();
        log.info("[{}] {}", chatId, text);

        try {
            var response = service.handle(text, chatId);
            if (Objects.nonNull(response)) {
                var state = service.getState(chatId);
                var buttons = state != null ? Arrays.asList(state.keyboardButtons()) : new ArrayList<String>();
                sendMessage(chatId, response, buttons);
            }
        } catch (Exception e) {
            log.error("Error", e);
            sendMessage(chatId, "❌ Something went wrong");
        }
    }

    private void sendMessage(Long chatId, String text, java.util.List<String> buttons) {
        try {
            var msg = new SendMessage(chatId.toString(), text);
            if (!buttons.isEmpty()) {
                var keyboard = new ArrayList<KeyboardRow>();
                var row = new KeyboardRow();
                for (var b : buttons) row.add(b);
                keyboard.add(row);
                msg.setReplyMarkup(ReplyKeyboardMarkup.builder().keyboard(keyboard).resizeKeyboard(true).build());
            }
            execute(msg);
        } catch (TelegramApiException e) { log.error("Send failed", e); }
    }

    private void sendMessage(Long chatId, String text) { sendMessage(chatId, text, new ArrayList<>()); }
}
