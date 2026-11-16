package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerateExerciseCommandTest {

    @Test
    void shouldAcceptCatalogTopic() {
        new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CatalogTopic("present-simple"), null, DictionaryId.generate(), "B1", Set.of());
    }

    @Test
    void shouldAcceptCustomTopic() {
        new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CustomTopic("Past Simple"), null, DictionaryId.generate(), "B1", Set.of());
    }

    @Test
    void shouldAcceptNullThematicTopic() {
        new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CatalogTopic("present-simple"), null, DictionaryId.generate(), "B1", Set.of());
    }

    @Test
    void shouldAcceptBlankThematicTopic() {
        new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CatalogTopic("present-simple"), "   ", DictionaryId.generate(), "B1", Set.of());
    }

    @Test
    void shouldRejectNullTopic() {
        assertThatThrownBy(() -> new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                null, null, DictionaryId.generate(), "B1", Set.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectBlankCatalogTopic() {
        assertThatThrownBy(() -> new CatalogTopic("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankCustomTopic() {
        assertThatThrownBy(() -> new CustomTopic("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullCefrLevel() {
        assertThatThrownBy(() -> new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CatalogTopic("present-simple"), null, DictionaryId.generate(), null, Set.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectBlankCefrLevel() {
        assertThatThrownBy(() -> new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CatalogTopic("present-simple"), null, DictionaryId.generate(), "  ", Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cefrLevel");
    }
}
