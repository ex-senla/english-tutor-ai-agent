package com.hydroyura.eta.chatbot.infrastructure.bot;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Продакшн-адаптер {@link ResponseSender}: тонкая делегация в {@code execute(...)}
 * {@code EnglishTutorBot}.
 *
 * <p>Использует {@link ObjectProvider} вместо прямого внедрения бота: это разрывает цикл
 * {@code EnglishTutorBot → ResponseSender → EnglishTutorBot} и резолвит <b>реальный</b> бин в
 * момент вызова (не CGLIB-прокси), поэтому {@code objectMapper} у {@code DefaultAbsSender}
 * гарантированно инициализирован.</p>
 */
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramResponseSender implements ResponseSender {

    private final ObjectProvider<EnglishTutorBot> botProvider;

    public TelegramResponseSender(ObjectProvider<EnglishTutorBot> botProvider) {
        this.botProvider = botProvider;
    }

    @Override
    public Message send(SendMessage message) throws TelegramApiException {
        return botProvider.getObject().execute(message);
    }

    @Override
    public void send(SendDocument document) throws TelegramApiException {
        botProvider.getObject().execute(document);
    }

    @Override
    public void edit(EditMessageText edit) throws TelegramApiException {
        botProvider.getObject().execute(edit);
    }

    @Override
    public void delete(DeleteMessage delete) throws TelegramApiException {
        botProvider.getObject().execute(delete);
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId) throws TelegramApiException {
        botProvider.getObject().execute(AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build());
    }
}
