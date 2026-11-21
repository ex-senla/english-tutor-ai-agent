package com.hydroyura.eta.support.domain.thread;

import com.hydroyura.eta.support.api.support.SupportAuthor;
import java.time.Instant;
import java.util.Objects;

public record SupportMessage(SupportAuthor author, String text, Instant createdAt) {

    public SupportMessage {
        Objects.requireNonNull(author, "SupportAuthor must not be null");
        Objects.requireNonNull(text, "Text must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
