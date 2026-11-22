package com.hydroyura.eta.chatbot.infrastructure.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Порт отправки ответов конвейера обработки {@code Update}.
 *
 * <p>Единственная точка исходящих вызовов {@code EnglishTutorBot.onUpdateReceived}.
 * Продакшн-адаптер ({@link TelegramResponseSender}) делегирует в {@code execute(...)}
 * {@code DefaultAbsSender}; тесты подменяют порт моком, не трогая Telegram API.</p>
 */
public interface ResponseSender {

    Message send(SendMessage message) throws TelegramApiException;

    void send(SendDocument document) throws TelegramApiException;

    void edit(EditMessageText edit) throws TelegramApiException;

    void delete(DeleteMessage delete) throws TelegramApiException;

    /**
     * Отвечает на callback query, чтобы клиент Telegram убрал индикатор загрузки с нажатой
     * inline-кнопки. Без этого нажатая кнопка остаётся подсвеченной, пока клиент не получит
     * ответ (см. issue #41).
     */
    void answerCallbackQuery(String callbackQueryId) throws TelegramApiException;
}
