package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;

final class ChatJpaMapper {

    private final ChatContextJson contextJson = new ChatContextJson();

    ChatJpa toEntity(Chat domain) {
        var entity = new ChatJpa();
        entity.chatId = domain.getId().chatId();
        sync(entity, domain);
        return entity;
    }

    void sync(ChatJpa entity, Chat domain) {
        entity.state = domain.getState();
        entity.context = contextJson.write(domain.getContext());
    }

    Chat toDomain(ChatJpa entity) {
        var chat = Chat.ofDefaults(new ChatId(entity.getChatId()));
        chat.updateState(entity.getState());
        chat.getContext().putAll(contextJson.read(entity.getContext()));
        return chat;
    }
}
