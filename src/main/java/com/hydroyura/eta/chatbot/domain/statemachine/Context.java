package com.hydroyura.eta.chatbot.domain.statemachine;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<String, Object> storage = new HashMap<>();

    public void put(String key, Object value) { storage.put(key, value); }
    public Object get(String key) { return storage.get(key); }
    public void remove(String key) { storage.remove(key); }
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) { return (T) storage.get(key); }
}
