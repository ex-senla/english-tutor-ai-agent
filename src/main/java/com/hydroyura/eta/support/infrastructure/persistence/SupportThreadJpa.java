package com.hydroyura.eta.support.infrastructure.persistence;

import com.hydroyura.eta.support.domain.thread.SupportThreadStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "support_thread")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportThreadJpa {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @Column(name = "teacher_id", nullable = false)
    UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    SupportThreadStatus status;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "last_message_at")
    Instant lastMessageAt;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    List<SupportMessageJpa> messages = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SupportThreadJpa other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
