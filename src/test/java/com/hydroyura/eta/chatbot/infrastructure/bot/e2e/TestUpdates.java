package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import java.util.List;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Фабрика {@link Update}-объектов четырёх видов, которые ожидает {@code UpdateParser}:
 * текст, команда ({@code bot_command} {@link MessageEntity}), reply-кнопка (обычный текст
 * из {@code Buttons.REPLY_BUTTONS}) и inline-callback ({@code prefix:payload}).
 */
public final class TestUpdates {

    private TestUpdates() {
    }

    public static Update text(Long chatId, String text) {
        return update(message(chatId, text, List.of()));
    }

    public static Update command(Long chatId, String command) {
        var entity = new MessageEntity();
        entity.setType("bot_command");
        entity.setOffset(0);
        entity.setLength(command.length());
        return update(message(chatId, command, List.of(entity)));
    }

    public static Update button(Long chatId, String buttonText) {
        return text(chatId, buttonText);
    }

    public static Update callback(Long chatId, int messageId, String data) {
        var message = new Message();
        message.setMessageId(messageId);
        message.setChat(chat(chatId));

        var callbackQuery = new CallbackQuery();
        callbackQuery.setData(data);
        callbackQuery.setMessage(message);

        var update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private static Update update(Message message) {
        var update = new Update();
        update.setMessage(message);
        return update;
    }

    private static Message message(Long chatId, String text, List<MessageEntity> entities) {
        var message = new Message();
        message.setChat(chat(chatId));
        message.setText(text);
        if (!entities.isEmpty()) {
            message.setEntities(entities);
        }
        return message;
    }

    private static Chat chat(Long chatId) {
        var chat = new Chat();
        chat.setId(chatId);
        return chat;
    }
}
