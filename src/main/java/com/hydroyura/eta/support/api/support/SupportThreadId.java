package com.hydroyura.eta.support.api.support;

import java.util.Objects;
import java.util.UUID;

public record SupportThreadId(UUID value) {

    public SupportThreadId {
        Objects.requireNonNull(value, "SupportThreadId must not be null");
    }

    public static SupportThreadId generate() {
        return new SupportThreadId(UUID.randomUUID());
    }
}
