package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class ChatService {

    private final ChatRepository repository;

    public Chat getOrCreate(Long chatId) {
        var id = new ChatId(chatId);
        return repository.findById(id).orElseGet(() -> create(id));
    }

    public void save(Chat chat) {
        repository.save(chat);
    }

    private Chat create(ChatId id) {
        log.info("create new stateMachine for id = '{}'", id);
        return Chat.ofDefaults(id);
    }
}
