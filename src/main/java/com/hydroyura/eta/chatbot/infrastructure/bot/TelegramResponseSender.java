package com.hydroyura.eta.chatbot.infrastructure.bot;

import org.springframework.context.annotation.Lazy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Продакшн-адаптер {@link ResponseSender}: тонкая делегация в {@code execute(...)}
 * {@code DefaultAbsSender} без изменения семантики.
 *
 * <p>{@code @Lazy} на параметре-боте разрывает цикл
 * {@code EnglishTutorBot → ResponseSender → EnglishTutorBot}.</p>
 */
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramResponseSender implements ResponseSender {

    private final DefaultAbsSender sender;

    public TelegramResponseSender(@Lazy EnglishTutorBot bot) {
        this.sender = bot;
    }

    @Override
    public Message send(SendMessage message) throws TelegramApiException {
        return sender.execute(message);
    }

    @Override
    public void send(SendDocument document) throws TelegramApiException {
        sender.execute(document);
    }

    @Override
    public void edit(EditMessageText edit) throws TelegramApiException {
        sender.execute(edit);
    }

    @Override
    public void delete(DeleteMessage delete) throws TelegramApiException {
        sender.execute(delete);
    }
}
