package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.BotResponse;
import com.hydroyura.eta.chatbot.application.StateMachineAppService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var msg = update.getMessage();
        var chatId = msg.getChatId();
        var text = msg.getText().trim();
        log.info("[{}] {}", chatId, text);
        processMessage(chatId, text);
    }

    private void handleCallback(Update update) {
        var callback = update.getCallbackQuery();
        var chatId = callback.getMessage().getChatId();
        var data = callback.getData();
        log.info("[{}] callback: {}", chatId, data);

        // Answer callback to remove loading indicator
        try {
            execute(new AnswerCallbackQuery(callback.getId()));
        } catch (TelegramApiException e) {
            log.error("Callback answer failed", e);
        }

        // Convert callback to text command and route through state machine
        if (data.startsWith("student:")) {
            var studentName = data.substring("student:".length());
            processMessage(chatId, "/student " + studentName);
        }
    }

    private void processMessage(Long chatId, String text) {
        try {
            var response = service.handle(text, chatId);
            if (Objects.nonNull(response)) {
                sendResponse(chatId, response);
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            sendMessage(chatId, "❌ Something went wrong");
        }
    }

    private void sendResponse(Long chatId, BotResponse response) {
        try {
            var msg = new SendMessage(chatId.toString(), response.text());

            if (response.hasInlineKeyboard()) {
                var rows = new ArrayList<List<InlineKeyboardButton>>();
                for (var row : response.inlineKeyboard()) {
                    var buttons = row.stream()
                        .map(btnText -> InlineKeyboardButton.builder()
                            .text(btnText)
                            .callbackData("student:" + btnText)
                            .build())
                        .toList();
                    rows.add(buttons);
                }
                msg.setReplyMarkup(new InlineKeyboardMarkup(rows));
            } else {
                var state = service.getState(chatId);
                var buttons = state != null ? Arrays.asList(state.keyboardButtons()) : new ArrayList<String>();
                if (!buttons.isEmpty()) {
                    var keyboardRows = new ArrayList<KeyboardRow>();
                    var row = new KeyboardRow();
                    for (var b : buttons) row.add(b);
                    keyboardRows.add(row);
                    msg.setReplyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboard(keyboardRows).resizeKeyboard(true).build());
                }
            }

            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Send failed", e);
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Send failed", e);
        }
    }
}
