package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.ChatService;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Регрессия issue #41: после нажатия inline-кнопки клиент Telegram держит её подсвеченной,
 * пока бот не ответит на callback query через {@code answerCallbackQuery}.
 */
class EnglishTutorBotCallbackAnswerTest {

    private static final Long CHAT_ID = 777L;

    private ResponseSender responseSender;

    private ChatService chatService;

    private UpdateParser updateParser;

    private SendMessageConverter converter;

    private StateMachine stateMachine;

    private EnglishTutorBot bot;

    @BeforeEach
    void setUp() {
        responseSender = mock(ResponseSender.class);
        chatService = mock(ChatService.class);
        updateParser = mock(UpdateParser.class);
        converter = mock(SendMessageConverter.class);
        stateMachine = mock(StateMachine.class);

        bot = new EnglishTutorBot("test-token", "test-bot", chatService, updateParser, converter, stateMachine,
                responseSender);

        when(chatService.getOrCreate(anyLong()))
                .thenReturn(mock(com.hydroyura.eta.chatbot.domain.chat.Chat.class));
        when(updateParser.parseUpdate(any())).thenReturn(new Action.Input("ignored"));
        when(stateMachine.applyAction(any(), any())).thenReturn(new ActionResult.TextResponse("ok"));
        when(converter.convert(any(), any()))
                .thenReturn(SendMessage.builder().chatId(CHAT_ID.toString()).text("ok").build());
    }

    @Test
    void shouldAnswerCallbackQueryOnCallbackUpdate() throws Exception {
        bot.onUpdateReceived(callbackUpdate("test-callback-id"));

        verify(responseSender).answerCallbackQuery("test-callback-id");
    }

    @Test
    void shouldNotAnswerCallbackQueryOnTextUpdate() throws Exception {
        bot.onUpdateReceived(textUpdate());

        verify(responseSender, never()).answerCallbackQuery(any());
    }

    private static Update callbackUpdate(String callbackQueryId) {
        var message = new Message();
        message.setMessageId(42);
        message.setChat(chat(CHAT_ID));

        var callbackQuery = new CallbackQuery();
        callbackQuery.setId(callbackQueryId);
        callbackQuery.setData("student:123");
        callbackQuery.setMessage(message);

        var update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private static Update textUpdate() {
        var message = new Message();
        message.setChat(chat(CHAT_ID));
        message.setText("hello");

        var update = new Update();
        update.setMessage(message);
        return update;
    }

    private static Chat chat(Long chatId) {
        var chat = new Chat();
        chat.setId(chatId);
        return chat;
    }
}
