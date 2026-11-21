package com.hydroyura.eta.support.infrastructure.persistence;

import com.hydroyura.eta.support.api.support.SupportAuthor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "support_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportMessageJpa {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "author", nullable = false, length = 16)
    SupportAuthor author;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    String text;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    SupportThreadJpa thread;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SupportMessageJpa other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
