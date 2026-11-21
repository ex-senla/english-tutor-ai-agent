package com.hydroyura.eta.exercise.infrastructure.persistence;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ExercisePersistenceRoundTripTest extends PostgresIntegrationTestBase {

    @Autowired
    private ExerciseRepository repository;

    @Test
    void shouldRoundTripExerciseWithItemsAndExpectedAnswers() {
        var wordA = WordInfoId.generate();
        var wordB = WordInfoId.generate();

        var exercise = Exercise.create(ExerciseId.generate(), ExerciseType.MULTIPLE_CHOICE, "Topic", "Travel",
                Set.of(wordA, wordB));
        exercise.setContent("content");
        exercise.setExpectedAnswers(List.of("run", "travel"));
        exercise.setItems(List.of(
                new ExerciseItem("I ___ every day.", List.of("run", "walk", "jump"), "run"),
                new ExerciseItem("She ___ a lot.", List.of("travel", "stay", "leave"), "travel")));
        exercise.markAnswered();
        repository.save(exercise);

        var loaded = repository.findById(exercise.getId()).orElseThrow();
        assertThat(loaded.getType()).isEqualTo(ExerciseType.MULTIPLE_CHOICE);
        assertThat(loaded.getTopic()).isEqualTo("Topic");
        assertThat(loaded.getThematicTopic()).isEqualTo("Travel");
        assertThat(loaded.getContent()).isEqualTo("content");
        assertThat(loaded.getStatus()).isEqualTo(ExerciseStatus.ANSWERED);
        assertThat(loaded.getExpectedAnswers()).containsExactly("run", "travel");
        assertThat(loaded.getItems()).hasSize(2);
        assertThat(loaded.getItems().get(0).options()).containsExactly("run", "walk", "jump");
        assertThat(loaded.getItems().get(1).correctAnswer()).isEqualTo("travel");
        assertThat(loaded.getWordInfoIds()).containsExactlyInAnyOrder(wordA, wordB);
    }

    @Test
    void shouldRoundTripNullableThematicTopic() {
        var withoutThematic = Exercise.create(ExerciseId.generate(), ExerciseType.FILL_IN_THE_BLANK, "Topic", null,
                Set.of());
        withoutThematic.setContent("content");
        withoutThematic.setExpectedAnswers(List.of("answer"));
        withoutThematic.setItems(List.of());
        withoutThematic.markChecked();
        repository.save(withoutThematic);

        var loaded = repository.findById(withoutThematic.getId()).orElseThrow();
        assertThat(loaded.getThematicTopic()).isNull();
        assertThat(loaded.getStatus()).isEqualTo(ExerciseStatus.CHECKED);
        assertThat(loaded.getExpectedAnswers()).containsExactly("answer");
    }
}
