package com.hydroyura.eta.support.domain.thread;

import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;

@Getter
@AggregateRoot
public class SupportThread {

    @Identity
    private SupportThreadId id;

    @Association
    private TeacherId teacherId;

    private SupportThreadStatus status;

    private Instant createdAt;

    private Instant lastMessageAt;

    private final List<SupportMessage> messages = new ArrayList<>();

    private SupportThread() {
    }

    public static SupportThread open(SupportThreadId id, TeacherId teacherId, Instant createdAt) {
        Objects.requireNonNull(id, "SupportThreadId must not be null");
        Objects.requireNonNull(teacherId, "TeacherId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        var thread = new SupportThread();
        thread.id = id;
        thread.teacherId = teacherId;
        thread.status = SupportThreadStatus.OPEN;
        thread.createdAt = createdAt;
        thread.lastMessageAt = null;
        return thread;
    }

    public static SupportThread reconstitute(SupportThreadId id, TeacherId teacherId, SupportThreadStatus status,
            Instant createdAt, Instant lastMessageAt, List<SupportMessage> messages) {
        var thread = open(id, teacherId, createdAt);
        thread.status = Objects.requireNonNull(status, "SupportThreadStatus must not be null");
        thread.lastMessageAt = lastMessageAt;
        thread.messages.addAll(messages);
        return thread;
    }

    public void appendMessage(SupportAuthor author, String text, Instant createdAt) {
        Objects.requireNonNull(author, "SupportAuthor must not be null");
        Objects.requireNonNull(text, "Text must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.messages.add(new SupportMessage(author, text, createdAt));
        this.lastMessageAt = createdAt;
    }

    public List<SupportMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
