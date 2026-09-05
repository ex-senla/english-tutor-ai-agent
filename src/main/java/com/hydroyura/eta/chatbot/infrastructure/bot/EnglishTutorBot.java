package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.ChatService;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.chatbot.application.statemachine.StateMachine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

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

        // 3. perform action in chat
        var oldState = chat.getState();
        var result = stateMachine.applyAction(chat, action);
        var newState = chat.getState();

        // 4. save chat
        chatService.save(chat);

        // 5. prepare response
        var response = converter.convert(result, chatId);

        // 6. delete old inline-keyboard message if requested
        if (result instanceof ActionResult.TextWithReplyKeyboard twk && twk.cleanupMessageId() != null) {
            var delete = DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(twk.cleanupMessageId())
                    .build();
            execute(delete);
        }

        // 7. send/edit/delete message
        // remove reply keyboard when leaving ACTIVE or IN_LESSON state
        if ((oldState == ChatState.ACTIVE && newState != ChatState.ACTIVE)
                || (oldState == ChatState.IN_LESSON && newState != ChatState.IN_LESSON)) {
            var remove = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(".")
                    .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                    .build();
            execute(remove);
        }
        switch (response) {
            case SendMessage msg -> execute(msg);
            case EditMessageText edit -> execute(edit);
            case DeleteMessage delete -> execute(delete);
            default -> throw new IllegalStateException("Unexpected response type: " + response.getClass());
        }
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
}
