package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerateExerciseCommandTest {

    @Test
    void shouldRejectNullCefrLevel() {
        assertThatThrownBy(() -> new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                "Past Simple", "Animals", DictionaryId.generate(), null, Set.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectBlankCefrLevel() {
        assertThatThrownBy(() -> new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                "Past Simple", "Animals", DictionaryId.generate(), "  ", Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cefrLevel");
    }
}
