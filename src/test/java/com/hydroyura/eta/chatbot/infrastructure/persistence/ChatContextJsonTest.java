package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.CustomTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatContextJsonTest {

    private final ChatContextJson converter = new ChatContextJson();

    @Test
    void shouldRoundTripAllRuntimeTypes() {
        var exerciseId = new ExerciseId(UUID.randomUUID());
        var context = new LinkedHashMap<String, Object>();
        context.put("string", "value");
        context.put("int", 42);
        context.put("str-list", List.of("a", "b"));
        context.put("str-set", Set.of("x", "y"));
        context.put("part-of-speech", PartOfSpeech.VERB);
        context.put("exercise-type", ExerciseType.FILL_IN_THE_BLANK);
        context.put("exercise-id", exerciseId);
        context.put("catalog-topic", new CatalogTopic("present-simple"));
        context.put("custom-topic", new CustomTopic("Past Simple"));

        var result = converter.read(converter.write(context));

        assertThat(result.get("string")).isEqualTo("value");
        assertThat(result.get("int")).isEqualTo(42);
        assertThat((List<String>) result.get("str-list")).containsExactly("a", "b");
        assertThat((Set<String>) result.get("str-set")).containsExactlyInAnyOrder("x", "y");
        assertThat(result.get("part-of-speech")).isEqualTo(PartOfSpeech.VERB);
        assertThat(result.get("exercise-type")).isEqualTo(ExerciseType.FILL_IN_THE_BLANK);
        assertThat(result.get("exercise-id")).isEqualTo(exerciseId);
        assertThat(result.get("catalog-topic")).isEqualTo(new CatalogTopic("present-simple"));
        assertThat(result.get("custom-topic")).isEqualTo(new CustomTopic("Past Simple"));
    }

    @Test
    void shouldSkipNullValues() {
        var context = new LinkedHashMap<String, Object>();
        context.put("nullable", null);
        context.put("string", "value");

        var result = converter.read(converter.write(context));

        assertThat(result).doesNotContainKey("nullable");
        assertThat(result.get("nullable")).isNull();
        assertThat(result.get("string")).isEqualTo("value");
    }

    @Test
    void shouldRoundTripEmptyContext() {
        assertThat(converter.read(converter.write(Map.of()))).isEmpty();
    }

    @Test
    void shouldFailFastOnUnknownValueType() {
        var context = Map.<String, Object>of("value", 1.5d);

        assertThatThrownBy(() -> converter.write(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported chat context value type");
    }

    @Test
    void shouldSkipUnknownTagOnRead() {
        var result = converter.read("{\"key\":{\"t\":\"unknown-tag\",\"v\":1}}");

        assertThat(result).isEmpty();
    }
}
