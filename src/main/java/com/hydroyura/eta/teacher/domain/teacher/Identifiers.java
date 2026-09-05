package com.hydroyura.eta.teacher.domain.teacher;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Identifiers {

    private final Map<IdentifierType, Object> values = new HashMap<>();

    public Identifiers() {
    }

    public <T> void put(IdentifierType type, T value) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
        if (!type.getValueClass().isInstance(value)) {
            throw new IllegalArgumentException(
                    "Expected " + type.getValueClass().getSimpleName() + " for " + type);
        }
        values.put(type, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(IdentifierType type) {
        return (T) values.get(type);
    }

    public Map<IdentifierType, Object> asMap() {
        return Collections.unmodifiableMap(values);
    }
}
