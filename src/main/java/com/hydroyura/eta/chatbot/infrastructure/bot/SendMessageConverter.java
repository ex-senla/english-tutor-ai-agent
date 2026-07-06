package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.action.ActionResult.InlineButton;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public final class SendMessageConverter {

    public Object convert(ActionResult result, Long chatId) {
        return switch (result) {
            case ActionResult.TextResponse(var text) -> buildText(chatId, text);
            case ActionResult.TextWithInlineKeyboard(var text, var keyboard) -> buildInline(chatId, text, keyboard);
            case ActionResult.TextWithReplyKeyboard(var text, var keyboard) -> buildReply(chatId, text, keyboard);
            case ActionResult.EditMessageText(var messageId, var text, var keyboard) -> buildEdit(chatId, messageId, text, keyboard);
            case ActionResult.DeleteMessage(var messageId) -> buildDelete(chatId, messageId);
        };
    }

    private SendMessage buildText(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
    }

    private SendMessage buildInline(Long chatId, String text, List<List<InlineButton>> keyboard) {
        var telegramKeyboard = keyboard.stream()
                .map(row -> row.stream()
                        .map(btn -> InlineKeyboardButton.builder()
                                .text(btn.text())
                                .callbackData(btn.callbackData())
                                .build())
                        .toList())
                .map(row -> (List<InlineKeyboardButton>) row)
                .toList();

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(new InlineKeyboardMarkup(telegramKeyboard))
                .build();
    }

    private SendMessage buildReply(Long chatId, String text, List<List<String>> keyboard) {
        var telegramKeyboard = keyboard.stream()
                .map(row -> {
                    var keyboardRow = new KeyboardRow();
                    row.forEach(keyboardRow::add);
                    return keyboardRow;
                })
                .toList();

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(new ReplyKeyboardMarkup(telegramKeyboard))
                .build();
    }

    private EditMessageText buildEdit(Long chatId, int messageId, String text, List<List<InlineButton>> keyboard) {
        var telegramKeyboard = keyboard.stream()
                .map(row -> row.stream()
                        .map(btn -> InlineKeyboardButton.builder()
                                .text(btn.text())
                                .callbackData(btn.callbackData())
                                .build())
                        .toList())
                .map(row -> (List<InlineKeyboardButton>) row)
                .toList();

        return EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(text)
                .replyMarkup(new InlineKeyboardMarkup(telegramKeyboard))
                .build();
    }

    private DeleteMessage buildDelete(Long chatId, int messageId) {
        return DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();
    }
}
