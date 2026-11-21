package com.hydroyura.eta.support.infrastructure.persistence;

import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.domain.thread.SupportThread;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaSupportThreadRepository implements SupportThreadRepository {

    private final SupportDataRepository dataRepository;

    private final SupportThreadJpaMapper mapper = new SupportThreadJpaMapper();

    private final EntityManager entityManager;

    public JpaSupportThreadRepository(SupportDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public SupportThread save(SupportThread thread) {
        var id = thread.getId().value();
        var existing = dataRepository.findWithMessagesById(id);
        if (existing.isEmpty()) {
            entityManager.persist(mapper.toEntity(thread));
        } else {
            mapper.sync(existing.get(), thread);
        }
        return thread;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SupportThread> findById(SupportThreadId id) {
        return dataRepository.findWithMessagesById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SupportThread> findByTeacherId(TeacherId teacherId) {
        return dataRepository.findByTeacherIdWithMessages(teacherId.value()).stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportThread> findAll() {
        return dataRepository.findAllWithMessages().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
