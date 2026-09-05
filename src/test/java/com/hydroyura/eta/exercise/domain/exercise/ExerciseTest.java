package com.hydroyura.eta.exercise.domain.exercise;

import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseTest {

    @Test
    void shouldCreateExercise() {
        var exercise = Exercise.create(ExerciseId.generate(), ExerciseType.FILL_IN_THE_BLANK, "Animals", Set.of(WordId
                .generate()));
        assertThat(exercise.getId()).isNotNull();
        assertThat(exercise.getType()).isEqualTo(ExerciseType.FILL_IN_THE_BLANK);
        assertThat(exercise.getTopic()).isEqualTo("Animals");
        assertThat(exercise.getWordIds()).hasSize(1);
        assertThat(exercise.getStatus()).isEqualTo(ExerciseStatus.GENERATED);
    }
}
