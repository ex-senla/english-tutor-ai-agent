package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.chatbot.infrastructure.bot.EnglishTutorBot;
import com.hydroyura.eta.chatbot.infrastructure.bot.ResponseSender;
import java.util.List;
import java.util.Map;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Фасад E2E-прогона конвейера бота, привязанный к {@code chatId} теста: приём
 * {@link Update}, удобные фабрики команд/текста/кнопок/коллбэков, доступ к перехваченным
 * исходящим вызовам и персистентному состоянию.
 */
public final class BotTester {

    private final EnglishTutorBot bot;

    private final Long chatId;

    private final OutgoingCallsRecorder recorder;

    private final BotDbInspector db;

    public BotTester(EnglishTutorBot bot, ResponseSender responseSender, BotDbInspector db, Long chatId)
            throws TelegramApiException {
        this.bot = bot;
        this.chatId = chatId;
        this.db = db;
        this.recorder = new OutgoingCallsRecorder();
        this.recorder.install(responseSender);
    }

    public void receive(Update update) {
        bot.onUpdateReceived(update);
    }

    public void command(String command) {
        receive(TestUpdates.command(chatId, command));
    }

    public void text(String text) {
        receive(TestUpdates.text(chatId, text));
    }

    public void button(String buttonText) {
        receive(TestUpdates.button(chatId, buttonText));
    }

    public void callback(String data) {
        callback(lastMessageId(), data);
    }

    public void callback(int messageId, String data) {
        receive(TestUpdates.callback(chatId, messageId, data));
    }

    public int lastMessageId() {
        return recorder.lastMessageId();
    }

    public List<BotCall> calls() {
        return recorder.calls();
    }

    public List<BotCall.SentMessage> messages() {
        return recorder.messages();
    }

    public List<BotCall.SentDocument> documents() {
        return recorder.documents();
    }

    public List<BotCall.EditedMessage> edits() {
        return recorder.edits();
    }

    public List<BotCall.DeletedMessage> deletes() {
        return recorder.deletes();
    }

    public BotCall.SentMessage lastMessage() {
        return recorder.lastMessage();
    }

    public String chatState() {
        return db.chatState(chatId);
    }

    public Map<String, Object> chatContext() {
        return db.chatContext(chatId);
    }

    public List<Map<String, Object>> rows(String table) {
        return db.rows(table);
    }

    public int count(String table) {
        return db.count(table);
    }

    public void reset() {
        recorder.reset();
    }
}
