package com.hydroyura.eta.exercise.application.topic;

import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseTopics;
import com.hydroyura.eta.exercise.api.exercise.TopicView;
import com.hydroyura.eta.exercise.domain.topic.GrammarTopic;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Загружает иерархический каталог грамматических тем из JSON-ресурса при старте и
 * предоставляет запросы по активному v1-подмножеству.
 *
 * <p>Загрузка fail-fast: дубликат slug, ссылка на несуществующего родителя или цикл
 * приводят к {@link IllegalStateException} в момент старта, а не в момент выбора темы.</p>
 */
@Slf4j
public class GrammarTopicCatalog implements ExerciseTopics {

    private static final String PATH_SEPARATOR = " › ";

    private static final int MAX_SUBTOPICS_IN_DISPLAY = 5;

    private final Map<String, GrammarTopic> bySlug;

    private final Map<String, List<GrammarTopic>> childrenByParent;

    private final List<GrammarTopic> roots;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GrammarTopicCatalog(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        var raw = load(resourcePath);
        var nodes = parse(raw);
        this.bySlug = indexBySlug(nodes);
        this.childrenByParent = groupChildren(nodes);
        this.roots = nodes.stream()
                .filter(topic -> topic.parentSlug() == null)
                .sorted(Comparator.comparingInt(GrammarTopic::sortOrder))
                .toList();
        validateIntegrity();
        warnOnLongCallbackPayloads();
    }

    private void warnOnLongCallbackPayloads() {
        for (var topic : activeTopics()) {
            for (var prefix : List.of("topicnav", "topic", "topiccatalog")) {
                var payload = prefix + ":" + topic.slug();
                if (payload.getBytes(StandardCharsets.UTF_8).length > 64) {
                    log.warn("Callback payload too long for topic '{}': {} bytes", topic.slug(),
                            payload.getBytes(StandardCharsets.UTF_8).length);
                }
            }
        }
    }

    private List<TopicJson> load(String resourcePath) {
        try (InputStream in = GrammarTopicCatalog.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Grammar topics resource not found: " + resourcePath);
            }
            var json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return List.of(objectMapper.readValue(json, TopicJson[].class));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load grammar topics resource: " + resourcePath, e);
        }
    }

    private static List<GrammarTopic> parse(List<TopicJson> jsonNodes) {
        return jsonNodes.stream()
                .map(TopicJson::toDomain)
                .toList();
    }

    private static Map<String, GrammarTopic> indexBySlug(List<GrammarTopic> nodes) {
        var index = new LinkedHashMap<String, GrammarTopic>();
        for (var node : nodes) {
            if (index.put(node.slug(), node) != null) {
                throw new IllegalStateException("Duplicate grammar topic slug: " + node.slug());
            }
        }
        return index;
    }

    private static Map<String, List<GrammarTopic>> groupChildren(List<GrammarTopic> nodes) {
        var map = new HashMap<String, List<GrammarTopic>>();
        for (var node : nodes) {
            if (node.parentSlug() != null) {
                map.computeIfAbsent(node.parentSlug(), key -> new ArrayList<>()).add(node);
            }
        }
        map.values().forEach(list -> list.sort(Comparator.comparingInt(GrammarTopic::sortOrder)));
        return map;
    }

    private void validateIntegrity() {
        for (var node : bySlug.values()) {
            if (node.parentSlug() != null && !bySlug.containsKey(node.parentSlug())) {
                throw new IllegalStateException("Grammar topic '%s' references missing parent '%s'"
                        .formatted(node.slug(), node.parentSlug()));
            }
        }
        for (var node : bySlug.values()) {
            if (hasCycle(node.slug(), new ArrayList<>())) {
                throw new IllegalStateException("Grammar topics tree contains a cycle at '%s'".formatted(node.slug()));
            }
        }
    }

    private boolean hasCycle(String slug, List<String> visited) {
        if (visited.contains(slug)) {
            return true;
        }
        visited.add(slug);
        var node = bySlug.get(slug);
        if (node != null && node.parentSlug() != null) {
            return hasCycle(node.parentSlug(), visited);
        }
        return false;
    }

    @Override
    public List<TopicView> roots() {
        return roots.stream()
                .filter(GrammarTopic::active)
                .map(this::toView)
                .toList();
    }

    @Override
    public List<TopicView> children(String slug) {
        var node = bySlug.get(slug);
        if (node == null) {
            return List.of();
        }
        return childrenByParent.getOrDefault(slug, List.of()).stream()
                .filter(GrammarTopic::active)
                .map(this::toView)
                .toList();
    }

    @Override
    public List<TopicView> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        var normalized = query.trim().toLowerCase(Locale.ROOT);
        return activeTopics().stream()
                .filter(topic -> topic.name() != null && topic.name().toLowerCase(Locale.ROOT).contains(normalized))
                .map(this::toView)
                .toList();
    }

    @Override
    public boolean exists(String slug) {
        var node = bySlug.get(slug);
        return node != null && node.active();
    }

    @Override
    public Optional<TopicView> find(String slug) {
        var node = bySlug.get(slug);
        if (node == null || !node.active()) {
            return Optional.empty();
        }
        return Optional.of(toView(node));
    }

    @Override
    public Optional<TopicView> parent(String slug) {
        var node = bySlug.get(slug);
        if (node == null || node.parentSlug() == null) {
            return Optional.empty();
        }
        var parent = bySlug.get(node.parentSlug());
        if (parent == null || !parent.active()) {
            return Optional.empty();
        }
        return Optional.of(toView(parent));
    }

    @Override
    public String path(String slug) {
        var node = bySlug.get(slug);
        if (node == null) {
            return "";
        }
        var names = new ArrayList<String>();
        var current = node;
        while (current != null) {
            names.add(0, current.name());
            current = current.parentSlug() == null ? null : bySlug.get(current.parentSlug());
        }
        return String.join(PATH_SEPARATOR, names);
    }

    /**
     * Строит отображаемую тему для генерации/PDF: путь каталога для предустановленной темы
     * (для не-листа — с пометкой «раздел» и ограниченным перечнем активных подтем) и
     * свободный текст для «своей темы».
     */
    public String displayTopic(ExerciseTopic topic) {
        Objects.requireNonNull(topic, "topic must not be null");
        if (topic instanceof CustomTopic custom) {
            return custom.name();
        }
        if (topic instanceof CatalogTopic catalog) {
            return displayCatalogTopic(catalog.slug());
        }
        throw new IllegalArgumentException("Unsupported ExerciseTopic: " + topic.getClass());
    }

    private String displayCatalogTopic(String slug) {
        var node = bySlug.get(slug);
        if (node == null) {
            return slug;
        }
        var path = path(slug);
        var activeChildren = activeChildrenNames(slug);
        if (activeChildren.isEmpty()) {
            return path;
        }
        var limited = activeChildren.stream()
                .limit(MAX_SUBTOPICS_IN_DISPLAY)
                .collect(Collectors.joining(", "));
        var suffix = activeChildren.size() > MAX_SUBTOPICS_IN_DISPLAY ? ", …" : "";
        return path + " (раздел: " + limited + suffix + ")";
    }

    private List<String> activeChildrenNames(String slug) {
        return childrenByParent.getOrDefault(slug, List.of()).stream()
                .filter(GrammarTopic::active)
                .map(GrammarTopic::name)
                .toList();
    }

    /**
     * Все активные темы в порядке обхода дерева (корни и их активные потомки).
     * Используется для построения компактного списка тем в LLM-валидации «Своей темы».
     */
    public List<GrammarTopic> activeTopics() {
        var result = new ArrayList<GrammarTopic>();
        for (var root : roots) {
            collectActive(root.slug(), result);
        }
        return result;
    }

    private void collectActive(String slug, List<GrammarTopic> result) {
        var node = bySlug.get(slug);
        if (node == null || !node.active()) {
            return;
        }
        result.add(node);
        for (var child : childrenByParent.getOrDefault(slug, List.of())) {
            collectActive(child.slug(), result);
        }
    }

    private TopicView toView(GrammarTopic topic) {
        return new TopicView(topic.slug(), topic.name(), topic.nameRu(),
                hasActiveChildren(topic.slug()));
    }

    private boolean hasActiveChildren(String slug) {
        return childrenByParent.getOrDefault(slug, List.of()).stream()
                .anyMatch(GrammarTopic::active);
    }

    private record TopicJson(
            String slug,
            String parentSlug,
            String name,
            String nameRu,
            String description,
            Integer sortOrder,
            Boolean isActive,
            Boolean paid
    ) {

        GrammarTopic toDomain() {
            Objects.requireNonNull(slug, "slug must not be null");
            Objects.requireNonNull(name, "name must not be null");
            return new GrammarTopic(
                    slug,
                    parentSlug,
                    name,
                    nameRu,
                    description,
                    sortOrder == null ? 0 : sortOrder,
                    Boolean.TRUE.equals(isActive),
                    Boolean.TRUE.equals(paid));
        }
    }
}
