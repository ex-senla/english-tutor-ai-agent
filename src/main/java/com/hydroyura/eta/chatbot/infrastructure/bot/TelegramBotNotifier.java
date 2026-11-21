package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.notification.BotNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Push-адаптер {@link BotNotifier}: отправляет текстовое сообщение в произвольный чат через
 * {@link ResponseSender}. Сбой Telegram не пробрасывается наружу (best-effort).
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramBotNotifier implements BotNotifier {

    private final ResponseSender responseSender;

    @Override
    public void notify(Long chatId, String text) {
        try {
            responseSender.send(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Failed to push support answer to chatId={}: {}", chatId, e.getMessage());
        }
    }
}
