package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import java.util.List;

public sealed interface BotCall
        permits BotCall.SentMessage, BotCall.SentDocument, BotCall.EditedMessage, BotCall.DeletedMessage {

    record SentMessage(int messageId, String text, KeyboardKind keyboard,
            List<List<BotButton>> inlineButtons, List<String> replyButtons) implements BotCall {
    }

    record SentDocument(String fileName, byte[] content, String caption) implements BotCall {
    }

    record EditedMessage(int messageId, String text, List<List<BotButton>> buttons) implements BotCall {
    }

    record DeletedMessage(int messageId) implements BotCall {
    }
}
