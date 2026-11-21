package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Типизированный конвертер {@code Chat.context} (Map&lt;String,Object&gt;) в jsonb-строку и обратно.
 *
 * <p>Каждый элемент сериализуется как {@code {"key": {"t": "<тег>", "v": <значение>}}}.
 * Ключи с {@code null}-значением пропускаются при записи. Неизвестный тип значения при записи —
 * {@link IllegalStateException} (fail-fast); неизвестный тег при чтении — WARN и пропуск ключа
 * (forward-совместимость).
 */
@Slf4j
public final class ChatContextJson {

    private static final String TAG = "t";

    private static final String VALUE = "v";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String write(Map<String, Object> context) {
        var root = new LinkedHashMap<String, Object>();
        context.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            var entry = new LinkedHashMap<String, Object>();
            entry.put(TAG, tagOf(value));
            entry.put(VALUE, encodeValue(value));
            root.put(key, entry);
        });
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize chat context", e);
        }
    }

    public Map<String, Object> read(String json) {
        var result = new LinkedHashMap<String, Object>();
        if (json == null || json.isBlank()) {
            return result;
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize chat context", e);
        }

        root.fields().forEachRemaining(entry -> {
            var tag = entry.getValue().path(TAG).asText();
            var valueNode = entry.getValue().path(VALUE);
            var decoded = decodeValue(tag, valueNode);
            if (decoded != null) {
                result.put(entry.getKey(), decoded);
            }
        });
        return result;
    }

    private String tagOf(Object value) {
        if (value instanceof String) {
            return "string";
        }
        if (value instanceof Integer) {
            return "int";
        }
        if (value instanceof List<?>) {
            return "str-list";
        }
        if (value instanceof Set<?>) {
            return "str-set";
        }
        if (value instanceof PartOfSpeech) {
            return "part-of-speech";
        }
        if (value instanceof ExerciseType) {
            return "exercise-type";
        }
        if (value instanceof ExerciseId) {
            return "exercise-id";
        }
        if (value instanceof CatalogTopic) {
            return "catalog-topic";
        }
        if (value instanceof CustomTopic) {
            return "custom-topic";
        }
        throw new IllegalStateException("Unsupported chat context value type: " + value.getClass().getName());
    }

    private Object encodeValue(Object value) {
        return switch (tagOf(value)) {
            case "exercise-id" -> ((ExerciseId) value).value().toString();
            case "catalog-topic" -> ((CatalogTopic) value).slug();
            case "custom-topic" -> ((CustomTopic) value).name();
            case "part-of-speech" -> ((PartOfSpeech) value).name();
            case "exercise-type" -> ((ExerciseType) value).name();
            default -> value;
        };
    }

    private Object decodeValue(String tag, JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return switch (tag) {
            case "string" -> node.asText();
            case "int" -> node.asInt();
            case "str-list" -> readList(node);
            case "str-set" -> readSet(node);
            case "part-of-speech" -> PartOfSpeech.valueOf(node.asText());
            case "exercise-type" -> ExerciseType.valueOf(node.asText());
            case "exercise-id" -> new ExerciseId(UUID.fromString(node.asText()));
            case "catalog-topic" -> new CatalogTopic(node.asText());
            case "custom-topic" -> new CustomTopic(node.asText());
            default -> {
                log.warn("Unknown chat context tag '{}', skipping key", tag);
                yield null;
            }
        };
    }

    private List<String> readList(JsonNode node) {
        var list = new ArrayList<String>();
        if (node.isArray()) {
            node.forEach(item -> list.add(item.asText()));
        }
        return list;
    }

    private Set<String> readSet(JsonNode node) {
        var set = new LinkedHashSet<String>();
        if (node.isArray()) {
            node.forEach(item -> set.add(item.asText()));
        }
        return set;
    }
}
