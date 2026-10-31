package com.hydroyura.eta.dictionary.api.word;

import java.util.Objects;
import java.util.UUID;

public record WordInfoId(UUID value) {

    public WordInfoId {
        Objects.requireNonNull(value, "WordId must not be null");
    }

    public static WordInfoId generate() {
        return new WordInfoId(UUID.randomUUID());
    }
}
