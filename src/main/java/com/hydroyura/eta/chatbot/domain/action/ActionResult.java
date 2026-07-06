package com.hydroyura.eta.chatbot.domain.action;

import java.util.List;

public sealed interface ActionResult
        permits ActionResult.TextResponse,
                ActionResult.TextWithInlineKeyboard,
                ActionResult.TextWithReplyKeyboard,
                ActionResult.EditMessageText,
                ActionResult.DeleteMessage {

    record TextResponse(String text) implements ActionResult {}

    record InlineButton(String text, String callbackData) {}

    record TextWithInlineKeyboard(String text, List<List<InlineButton>> keyboard) implements ActionResult {}

    record TextWithReplyKeyboard(String text, List<List<String>> keyboard) implements ActionResult {}

    record EditMessageText(int messageId, String text, List<List<InlineButton>> keyboard) implements ActionResult {}

    record DeleteMessage(int messageId) implements ActionResult {}
}
