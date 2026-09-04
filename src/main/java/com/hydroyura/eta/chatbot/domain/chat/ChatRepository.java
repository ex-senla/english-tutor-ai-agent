package com.hydroyura.eta.chatbot.domain.chat;

import java.util.Optional;

public interface ChatRepository {

    Optional<Chat> findById(ChatId id);

    void save(Chat chat);

}
