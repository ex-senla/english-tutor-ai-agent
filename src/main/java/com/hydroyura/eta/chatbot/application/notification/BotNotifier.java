package com.hydroyura.eta.chatbot.application.notification;

/**
 * Платформо-независимая точка «отправить сообщение в произвольный чат» вне конвейера Update.
 */
public interface BotNotifier {

    void notify(Long chatId, String text);
}
