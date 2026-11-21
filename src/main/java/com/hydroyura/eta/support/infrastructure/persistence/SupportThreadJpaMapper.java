package com.hydroyura.eta.support.infrastructure.persistence;

import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.domain.thread.SupportMessage;
import com.hydroyura.eta.support.domain.thread.SupportThread;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.UUID;

final class SupportThreadJpaMapper {

    SupportThreadJpa toEntity(SupportThread domain) {
        var entity = new SupportThreadJpa();
        entity.id = domain.getId().value();
        sync(entity, domain);
        return entity;
    }

    void sync(SupportThreadJpa entity, SupportThread domain) {
        entity.teacherId = domain.getTeacherId().value();
        entity.status = domain.getStatus();
        entity.createdAt = domain.getCreatedAt();
        entity.lastMessageAt = domain.getLastMessageAt();

        entity.getMessages().clear();
        domain.getMessages().forEach(message -> {
            var jpa = new SupportMessageJpa();
            jpa.id = UUID.randomUUID();
            jpa.author = message.author();
            jpa.text = message.text();
            jpa.createdAt = message.createdAt();
            jpa.thread = entity;
            entity.getMessages().add(jpa);
        });
    }

    SupportThread toDomain(SupportThreadJpa entity) {
        var messages = entity.getMessages().stream()
                .map(m -> new SupportMessage(m.getAuthor(), m.getText(), m.getCreatedAt()))
                .toList();
        return SupportThread.reconstitute(
                new SupportThreadId(entity.getId()),
                new TeacherId(entity.getTeacherId()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getLastMessageAt(),
                messages);
    }
}
