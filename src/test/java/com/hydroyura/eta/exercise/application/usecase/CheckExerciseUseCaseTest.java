package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseCommand;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CheckExerciseUseCaseTest {

    private CheckExerciseUseCase useCase;

    private StubExerciseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubExerciseRepository();
        useCase = new CheckExerciseUseCase(repository);
    }

    @Test
    void shouldCheckAllItemsCorrect() {
        var exercise = createExercise(List.of("travelled", "went"));
        repository.save(exercise);

        var result = useCase.execute(new CheckExerciseCommand(exercise.getId(), "travelled, went"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).contains("1) ✅").contains("2) ✅");
        assertThat(repository.findById(exercise.getId()).orElseThrow().getStatus())
                .isEqualTo(ExerciseStatus.CHECKED);
    }

    @Test
    void shouldMarkWrongItem() {
        var exercise = createExercise(List.of("travelled", "went"));
        repository.save(exercise);

        var result = useCase.execute(new CheckExerciseCommand(exercise.getId(), "travelled, gone"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("2) ❌").contains("went");
        assertThat(repository.findById(exercise.getId()).orElseThrow().getStatus())
                .isEqualTo(ExerciseStatus.ANSWERED);
    }

    @Test
    void shouldMarkWrongWhenAnswerCountDiffers() {
        var exercise = createExercise(List.of("travelled", "went"));
        repository.save(exercise);

        var result = useCase.execute(new CheckExerciseCommand(exercise.getId(), "travelled"));

        assertThat(result.correct()).isFalse();
    }

    @Test
    void shouldKeepPositionsWhenAnswerSkipped() {
        var exercise = createExercise(List.of("travelled", "went"));
        repository.save(exercise);

        var result = useCase.execute(new CheckExerciseCommand(exercise.getId(), "travelled,"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("1) ✅").contains("2) ❌");
    }

    @Test
    void shouldCompareAnswersIgnoringCaseAndWhitespace() {
        var exercise = createExercise(List.of("travelled", "went"));
        repository.save(exercise);

        var result = useCase.execute(new CheckExerciseCommand(exercise.getId(), "TRAVELLED,  WENT "));

        assertThat(result.correct()).isTrue();
    }

    private Exercise createExercise(List<String> expectedAnswers) {
        var exercise = Exercise.create(ExerciseId.generate(), ExerciseType.FILL_IN_THE_BLANK, "Animals", Set.of(WordId
                .generate()));
        exercise.setContent("1. Last summer, I ___ to the mountains.\n2. We ___ home late.");
        exercise.setExpectedAnswers(expectedAnswers);
        return exercise;
    }

    static class StubExerciseRepository implements ExerciseRepository {

        private final Map<ExerciseId, Exercise> store = new HashMap<>();

        @Override
        public Exercise save(Exercise exercise) {
            store.put(exercise.getId(), exercise);
            return exercise;
        }

        @Override
        public Optional<Exercise> findById(ExerciseId id) {
            return Optional.ofNullable(store.get(id));
        }
    }
}
