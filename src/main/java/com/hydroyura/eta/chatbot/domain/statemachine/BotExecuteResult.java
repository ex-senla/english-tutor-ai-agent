package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.List;

public record BotExecuteResult(String message, List<List<String>> inlineKeyboard) {}
