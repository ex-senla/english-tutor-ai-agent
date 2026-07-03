package com.hydroyura.eta.chatbot.application;

import java.util.List;

public record BotResponse(String text, List<List<String>> inlineKeyboard) {

    public BotResponse(String text) {
        this(text, List.of());
    }

    public boolean hasInlineKeyboard() {
        return !inlineKeyboard.isEmpty();
    }
}
