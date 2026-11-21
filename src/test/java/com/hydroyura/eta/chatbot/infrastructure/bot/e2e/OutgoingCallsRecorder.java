package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.infrastructure.bot.ResponseSender;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Перехватывает все вызовы мока {@link ResponseSender} и складывает их в общий список
 * в порядке вызова. {@code send(SendMessage)} возвращает синтетический {@link Message}
 * с монотонно растущим {@code messageId}.
 */
public final class OutgoingCallsRecorder {

    private final List<BotCall> calls = new ArrayList<>();

    private int nextMessageId = 1;

    private int lastMessageId = 0;

    public void install(ResponseSender mock) throws TelegramApiException {
        doAnswer(invocation -> {
            var message = invocation.getArgument(0, SendMessage.class);
            var id = nextMessageId++;
            lastMessageId = id;
            calls.add(toSentMessage(id, message));
            return syntheticMessage(id);
        }).when(mock).send(any(SendMessage.class));

        doAnswer(invocation -> {
            var document = invocation.getArgument(0, SendDocument.class);
            calls.add(toSentDocument(document));
            return null;
        }).when(mock).send(any(SendDocument.class));

        doAnswer(invocation -> {
            var edit = invocation.getArgument(0, EditMessageText.class);
            calls.add(toEditedMessage(edit));
            return null;
        }).when(mock).edit(any(EditMessageText.class));

        doAnswer(invocation -> {
            var delete = invocation.getArgument(0, DeleteMessage.class);
            calls.add(new BotCall.DeletedMessage(delete.getMessageId()));
            return null;
        }).when(mock).delete(any(DeleteMessage.class));
    }

    public List<BotCall> calls() {
        return List.copyOf(calls);
    }

    public List<BotCall.SentMessage> messages() {
        return calls.stream()
                .filter(BotCall.SentMessage.class::isInstance)
                .map(BotCall.SentMessage.class::cast)
                .toList();
    }

    public List<BotCall.SentDocument> documents() {
        return calls.stream()
                .filter(BotCall.SentDocument.class::isInstance)
                .map(BotCall.SentDocument.class::cast)
                .toList();
    }

    public List<BotCall.EditedMessage> edits() {
        return calls.stream()
                .filter(BotCall.EditedMessage.class::isInstance)
                .map(BotCall.EditedMessage.class::cast)
                .toList();
    }

    public List<BotCall.DeletedMessage> deletes() {
        return calls.stream()
                .filter(BotCall.DeletedMessage.class::isInstance)
                .map(BotCall.DeletedMessage.class::cast)
                .toList();
    }

    public BotCall.SentMessage lastMessage() {
        var messages = messages();
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public int lastMessageId() {
        return lastMessageId;
    }

    public void reset() {
        calls.clear();
        nextMessageId = 1;
        lastMessageId = 0;
    }

    private static BotCall.SentMessage toSentMessage(int id, SendMessage message) {
        var chatId = Long.valueOf(message.getChatId());
        var markup = message.getReplyMarkup();
        if (markup instanceof ReplyKeyboardMarkup replyKeyboardMarkup) {
            var replyButtons = replyKeyboardMarkup.getKeyboard().stream()
                    .flatMap(row -> row.stream().map(KeyboardButton::getText))
                    .toList();
            return new BotCall.SentMessage(id, message.getText(), KeyboardKind.REPLY, List.of(), replyButtons, chatId);
        }
        if (markup instanceof InlineKeyboardMarkup inlineKeyboardMarkup) {
            var inlineButtons = inlineKeyboardMarkup.getKeyboard().stream()
                    .map(row -> row.stream()
                            .map(button -> new BotButton(button.getText(), button.getCallbackData()))
                            .toList())
                    .toList();
            return new BotCall.SentMessage(id, message.getText(), KeyboardKind.INLINE, inlineButtons, List.of(),
                    chatId);
        }
        return new BotCall.SentMessage(id, message.getText(), KeyboardKind.NONE, List.of(), List.of(), chatId);
    }

    private static BotCall.SentDocument toSentDocument(SendDocument document) {
        var inputFile = document.getDocument();
        var fileName = inputFile.getMediaName() != null ? inputFile.getMediaName() : inputFile.getAttachName();
        return new BotCall.SentDocument(fileName, readBytes(inputFile.getNewMediaStream()), document.getCaption());
    }

    private static BotCall.EditedMessage toEditedMessage(EditMessageText edit) {
        var markup = edit.getReplyMarkup();
        var buttons = markup == null
                ? List.<List<BotButton>>of()
                : markup.getKeyboard().stream()
                        .map(row -> row.stream()
                                .map(button -> new BotButton(button.getText(), button.getCallbackData()))
                                .toList())
                        .toList();
        return new BotCall.EditedMessage(edit.getMessageId(), edit.getText(), buttons);
    }

    private static byte[] readBytes(InputStream in) {
        if (in == null) {
            return new byte[0];
        }
        try (in) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Message syntheticMessage(int id) {
        var message = new Message();
        message.setMessageId(id);
        return message;
    }
}
