package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.shared.api.SnapshotProvider;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Repository
// TODO: remove SnapshotProvider when switching to JPA/PostgreSQL
public class InMemoryChatRepository implements ChatRepository, SnapshotProvider {

    private final Map<ChatId, Chat> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Chat> findById(ChatId id) {
        var chat = store.get(id);
        if (chat == null) {
            chat = Chat.ofDefaults(id);
            store.put(id, chat);
        }
        return Optional.of(chat);
    }

    @Override
    public void save(Chat chat) {
        store.put(chat.getId(), chat);
    }

    // TODO: remove when switching to JPA/PostgreSQL
    public Map<ChatId, Chat> snapshot() {
        return Map.copyOf(store);
    }
}
