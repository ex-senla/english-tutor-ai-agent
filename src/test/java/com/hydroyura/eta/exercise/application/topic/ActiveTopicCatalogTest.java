package com.hydroyura.eta.exercise.application.topic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Сверка активного v1-подмножества с эталоном аналитики и fail-soft проверка
 * покрытия активных листовых тем промпт-фрагментами.
 */
class ActiveTopicCatalogTest {

    private static final Set<String> EXPECTED_ACTIVE_LEAVES = Set.of(
            "present-simple", "present-continuous", "present-perfect",
            "past-simple", "future-simple", "be-going-to",
            "countable-uncountable-nouns", "singular-plural-nouns",
            "comparative-adjectives", "superlative-adjectives",
            "adverbs-of-frequency", "prepositions-of-time",
            "indefinite-article", "definite-article",
            "some-any", "much-many",
            "modal-ability", "modal-obligation", "modal-advice",
            "zero-conditional", "first-conditional", "second-conditional",
            "yes-no-questions", "wh-questions");

    @Test
    void shouldContainExpectedActiveLeafSlugs() {
        var catalog = new GrammarTopicCatalog("/exercise/topics/grammar-topics.json");
        var activeLeaves = catalog.activeTopics().stream()
                .filter(topic -> catalog.children(topic.slug()).isEmpty())
                .map(topic -> topic.slug())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(activeLeaves).isEqualTo(EXPECTED_ACTIVE_LEAVES);
    }

    @Test
    void activeTopicCallbacksShouldFitTelegramPayloadLimit() {
        var catalog = new GrammarTopicCatalog("/exercise/topics/grammar-topics.json");
        var tooLong = catalog.activeTopics().stream()
                .flatMap(topic -> java.util.List.of("topicnav:" + topic.slug(), "topic:" + topic.slug(),
                        "topiccatalog:" + topic.slug()).stream())
                .filter(payload -> payload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 64)
                .toList();

        assertThat(tooLong).isEmpty();
    }

    @Test
    void shouldHavePromptFragmentsForActiveLeafSlugs() {
        var catalog = new GrammarTopicCatalog("/exercise/topics/grammar-topics.json");
        var missing = catalog.activeTopics().stream()
                .filter(topic -> catalog.children(topic.slug()).isEmpty())
                .map(topic -> topic.slug())
                .filter(slug -> loadFragment(slug) == null)
                .toList();

        assertThat(missing)
                .as("prompt fragments must exist for every active leaf topic; missing: %s", missing)
                .isEmpty();
    }

    private String loadFragment(String slug) {
        var path = "/exercise/topics/prompts/" + slug + ".txt";
        try (var in = ActiveTopicCatalogTest.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
