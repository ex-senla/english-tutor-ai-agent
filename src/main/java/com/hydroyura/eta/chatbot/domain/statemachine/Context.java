package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Context {

    private final Map<String, Object> storage = new HashMap<>();


    public <T> Optional<T> getSafely(String key, Class<T> clazz) {
        return Optional
                .ofNullable(storage.get(key))
                .map(clazz::cast);
    }

    public void put(String key, Object value) {
        storage.put(key, value);
    }

}
