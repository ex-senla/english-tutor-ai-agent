package com.hydroyura.eta.chatbot.infrastructure.bot;

import com.hydroyura.eta.chatbot.application.ActionHandler;
import com.hydroyura.eta.chatbot.application.StateMachineAppService;
import com.hydroyura.eta.chatbot.domain.statemachine.State;
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
    private final StateMachineAppService service;
    private final UpdateParser updateParser;
    private final ActionHandler actionHandler;
    private final SendMessageConverter converter;

    public EnglishTutorBot(@Value("${telegram.bot.token}") String botToken,
                           @Value("${telegram.bot.username}") String botUsername,
                           StateMachineAppService service, UpdateParser updateParser,
                           ActionHandler actionHandler, SendMessageConverter converter) {
        super(botToken);
        this.botUsername = botUsername;
        this.service = service;
        this.updateParser = updateParser;
        this.actionHandler = actionHandler;
        this.converter = converter;
    }

    @Override
    public String getBotUsername() { return botUsername; }

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
        // 1. get stateMachine
        var sm = service.getOrCreate(chatId);

        // 2. parse update to select action
        var action = updateParser.parseUpdate(update);

        // 3. perform action in sm
        var oldState = sm.getState();
        var result = actionHandler.handle(sm, action);
        var newState = sm.getState();

        // 4. save sm
        service.save(sm);

        // 5. prepare response
        var response = converter.convert(result, chatId);

        // 6. send/edit/delete message
        // remove reply keyboard when leaving ACTIVE state
        if (oldState == State.ACTIVE && newState != State.ACTIVE) {
            var remove = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("\u200B")
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
