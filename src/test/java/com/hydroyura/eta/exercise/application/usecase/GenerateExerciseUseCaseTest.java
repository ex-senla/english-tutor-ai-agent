package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerateExerciseUseCaseTest {

    @Test
    void shouldPassAllWordsToGenerator() {
        var dictId = DictionaryId.generate();
        var appleId = WordId.generate();
        var runId = WordId.generate();
        var bigId = WordId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW),
                new WordProjection(runId, "run", Set.of("бежать"), PartOfSpeech.VERB, WordStatus.IN_PROGRESS),
                new WordProjection(bigId, "big", Set.of("большой"), PartOfSpeech.ADJECTIVE, WordStatus.LEARNED)));

        var generator = new RecordingGenerator();
        var repository = new StubExerciseRepository();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Animals", dictId,
                "A2");
        useCase.execute(command);

        assertThat(generator.receivedWords).extracting(WordData::value)
                .containsExactlyInAnyOrder("apple", "run", "big");

        var saved = repository.saved;
        assertThat(saved.getWordIds()).containsExactlyInAnyOrder(appleId, runId, bigId);
    }

    @Test
    void shouldThrowWhenDictionaryEmpty() {
        var dictId = DictionaryId.generate();
        var findWords = new StubFindWords(Set.of());

        var useCase = new GenerateExerciseUseCase(new StubExerciseRepository(), new RecordingGenerator(), findWords);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Animals", dictId,
                "A2");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dictionary");
    }

    static class RecordingGenerator implements ExerciseGenerator {

        Set<WordData> receivedWords;

        @Override
        public ExerciseDto generate(GenerateExerciseCommand command, Set<WordData> words) {
            this.receivedWords = words;
            return new ExerciseDto(ExerciseId.generate(), command.type(), command.topic(), "content",
                    List.of("answer"), List.of(), ExerciseStatus.GENERATED);
        }
    }

    static class StubFindWords implements FindWords {

        private final Set<WordProjection> projections;

        StubFindWords(Set<WordProjection> projections) {
            this.projections = projections;
        }

        @Override
        public Set<WordProjection> findByDictionaryId(DictionaryId dictionaryId) {
            return projections;
        }

        @Override
        public DictionaryStats getStats(DictionaryId dictionaryId) {
            return new DictionaryStats(projections.size(), 0, 0, 0);
        }
    }

    static class StubExerciseRepository implements ExerciseRepository {

        Exercise saved;

        @Override
        public Exercise save(Exercise exercise) {
            this.saved = exercise;
            return exercise;
        }

        @Override
        public Optional<Exercise> findById(ExerciseId id) {
            return Optional.empty();
        }
    }
}
