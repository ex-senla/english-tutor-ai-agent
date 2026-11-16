package com.hydroyura.eta.exercise.application.topic;

import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrammarTopicCatalogTest {

    private static final String VALID = "/exercise/topics/test-grammar-topics.json";

    @Test
    void shouldLoadRootsSortedBySortOrder() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.roots()).extracting(t -> t.slug())
                .containsExactly("tenses-and-aspects");
    }

    @Test
    void shouldExposeOnlyActiveNodes() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.children("tenses-and-aspects")).extracting(t -> t.slug())
                .containsExactly("present", "past");
        assertThat(catalog.exists("past-simple")).isTrue();
        assertThat(catalog.exists("past-perfect-continuous")).isFalse();
    }

    @Test
    void shouldSearchByNameCaseInsensitively() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.search("simple")).extracting(t -> t.slug())
                .containsExactlyInAnyOrder("present-simple", "past-simple");
        assertThat(catalog.search("SIMPLE")).isNotEmpty();
        assertThat(catalog.search("perfect continuous")).isEmpty();
    }

    @Test
    void shouldBuildPathFromRoot() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.path("present-simple")).isEqualTo("Tenses and Aspects › Present › Present Simple");
    }

    @Test
    void shouldDisplayLeafTopicAsPath() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.displayTopic(new CatalogTopic("present-simple")))
                .isEqualTo("Tenses and Aspects › Present › Present Simple");
    }

    @Test
    void shouldDisplayNonLeafTopicWithSectionMarkerAndSubtopicList() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.displayTopic(new CatalogTopic("present")))
                .startsWith("Tenses and Aspects › Present (раздел: ")
                .contains("Present Simple", "Present Continuous");
    }

    @Test
    void shouldDisplayCustomTopicAsIs() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.displayTopic(new CustomTopic("Past Simple"))).isEqualTo("Past Simple");
    }

    @Test
    void shouldReturnParentForNonRoot() {
        var catalog = new GrammarTopicCatalog(VALID);

        assertThat(catalog.parent("present-simple")).isPresent();
        assertThat(catalog.parent("present-simple").get().slug()).isEqualTo("present");
        assertThat(catalog.parent("tenses-and-aspects")).isEmpty();
    }

    @Test
    void shouldDetectDuplicateSlug() {
        assertThatThrownBy(() -> new GrammarTopicCatalog("/exercise/topics/duplicate-slug.json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void shouldDetectBrokenParent() {
        assertThatThrownBy(() -> new GrammarTopicCatalog("/exercise/topics/broken-parent.json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("parent");
    }

    @Test
    void shouldDetectCycle() {
        assertThatThrownBy(() -> new GrammarTopicCatalog("/exercise/topics/cycle.json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }
}
