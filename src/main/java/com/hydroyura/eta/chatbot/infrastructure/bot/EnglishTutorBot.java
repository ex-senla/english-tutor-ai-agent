package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.ChatService;
import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import static com.hydroyura.eta.chatbot.view.Messages.GENERATING_EXERCISE;

@Slf4j
@Component
public class EnglishTutorBot extends TelegramLongPollingBot {

    private final String botUsername;

    private final ChatService chatService;

    private final UpdateParser updateParser;

    private final SendMessageConverter converter;

    private final StateMachine stateMachine;

    public EnglishTutorBot(@Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            ChatService chatService, UpdateParser updateParser,
            SendMessageConverter converter, StateMachine stateMachine) {
        super(botToken);
        this.botUsername = botUsername;
        this.chatService = chatService;
        this.updateParser = updateParser;
        this.converter = converter;
        this.stateMachine = stateMachine;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    @SneakyThrows // TODO: create exception handler
    public void onUpdateReceived(Update update) {
        // skip non-actionable updates (bot kicked, chat member changes, etc.)
        if (update.hasMyChatMember()) {
            var status = update.getMyChatMember().getNewChatMember().getStatus();
            var chatId = update.getMyChatMember().getChat().getId();
            log.info("my_chat_member update: chatId={}, status={}", chatId, status);
            return;
        }

        // 0. get chatId
        var chatId = extractChatId(update);
        // 1. get chat
        var chat = chatService.getOrCreate(chatId);

        // 2. parse update to select action
        var action = updateParser.parseUpdate(update);

        // 2.1 показать индикатор длительной обработки (если предстоит долгая операция)
        var progressMessageId = showProcessingIndicator(chat, action, chatId);

        // 3. perform action in chat
        var oldState = chat.getState();
        var result = stateMachine.applyAction(chat, action);
        var newState = chat.getState();

        // 3.1 удалить сообщение-индикатор после завершения обработки
        deleteProcessingIndicator(chatId, progressMessageId);

        // 4. save chat
        chatService.save(chat);

        // 5. prepare response
        // (вычисляется ниже, после обработки батч-отправки документов)

        // 6. delete old inline-keyboard message if requested
        if (result instanceof ActionResult.TextWithReplyKeyboard twk && twk.cleanupMessageId() != null) {
            var delete = DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(twk.cleanupMessageId())
                    .build();
            execute(delete);
        }

        // 7. remove reply keyboard when leaving ACTIVE or IN_LESSON state
        if (isFromStateWithReplyKeyboard(oldState, newState)) {
            var remove = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("...")
                    .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                    .build();
            var removed = execute(remove);
            execute(converter.buildDelete(chatId, removed.getMessageId()));
        }

        // 8. send documents / send/edit/delete message
        if (result instanceof ActionResult.SendDocuments sendDocuments) {
            for (var doc : sendDocuments.documents()) {
                execute(converter.convertDocument(doc, chatId));
            }
            execute(converter.convertTextWithInlineKeyboard(sendDocuments.text(), sendDocuments.keyboard(), chatId));
            return;
        }
        var response = converter.convert(result, chatId);
        switch (response) {
            case SendMessage msg -> execute(msg);
            case EditMessageText edit -> execute(edit);
            case DeleteMessage delete -> execute(delete);
            case SendDocument doc -> execute(doc);
            default -> throw new IllegalStateException("Unexpected response type: " + response.getClass());
        }
    }

    private static boolean isFromStateWithReplyKeyboard(ChatState oldState, ChatState newState) {
        return (oldState == ChatState.ACTIVE && newState != ChatState.ACTIVE)
                || (oldState == ChatState.IN_LESSON && newState != ChatState.IN_LESSON);
    }

    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        throw new IllegalArgumentException("Update has neither message nor callback query");
    }

    /**
     * Показывает в чате сообщение-индикатор длительной операции (например, «⏳ Генерирую…»),
     * возвращая идентификатор отправленного сообщения, чтобы удалить его после завершения.
     * Возвращает {@code null}, если индикатор не требуется.
     */
    private Integer showProcessingIndicator(Chat chat, Action action, Long chatId) {
        if (chat.getState() != ChatState.AWAITING_EXERCISE_TOPIC || !(action instanceof Action.Input)) {
            return null;
        }
        try {
            var sent = execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(GENERATING_EXERCISE)
                    .build());
            return sent.getMessageId();
        } catch (Exception e) {
            log.warn("Failed to send generation indicator for chatId={}: {}", chatId, e.getMessage());
            return null;
        }
    }

    /**
     * Удаляет сообщение-индикатор, если оно было отправлено.
     */
    private void deleteProcessingIndicator(Long chatId, Integer messageId) {
        if (messageId == null) {
            return;
        }
        try {
            execute(DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete generation indicator for chatId={}, messageId={}: {}",
                    chatId, messageId, e.getMessage());
        }
    }
}
