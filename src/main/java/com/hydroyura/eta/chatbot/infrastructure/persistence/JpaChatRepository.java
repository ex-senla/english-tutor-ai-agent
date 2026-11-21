package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaChatRepository implements ChatRepository {

    private final ChatDataRepository dataRepository;

    private final ChatJpaMapper mapper = new ChatJpaMapper();

    private final EntityManager entityManager;

    public JpaChatRepository(ChatDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chat> findById(ChatId id) {
        return dataRepository.findById(id.chatId()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void save(Chat chat) {
        var id = chat.getId().chatId();
        var existing = dataRepository.findById(id);
        if (existing.isEmpty()) {
            entityManager.persist(mapper.toEntity(chat));
        } else {
            mapper.sync(existing.get(), chat);
        }
    }
}
