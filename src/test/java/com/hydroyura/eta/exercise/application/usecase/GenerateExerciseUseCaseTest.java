package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.IncrementWordRepetitions;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateExerciseUseCaseTest {

    @Test
    void shouldGenerateOnlyFromSelectedWordIds() {
        var dictId = DictionaryId.generate();
        var appleId = WordInfoId.generate();
        var runId = WordInfoId.generate();
        var bigId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW),
                new WordProjection(runId, "run", Set.of("бежать"), PartOfSpeech.VERB, WordStatus.IN_PROGRESS),
                new WordProjection(bigId, "big", Set.of("большой"), PartOfSpeech.ADJECTIVE, WordStatus.LEARNED)));

        var generator = new RecordingGenerator(exerciseDto(List.of()));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Animals", dictId,
                "A2", Set.of(appleId, runId));
        useCase.execute(command);

        assertThat(generator.receivedWords).extracting(WordData::value)
                .containsExactlyInAnyOrder("apple", "run");
        assertThat(repository.saved.getWordInfoIds()).containsExactlyInAnyOrder(appleId, runId);
        assertThat(increment.incrementedIds).isEmpty();
    }

    @Test
    void shouldGenerateWithEmptySelection() {
        var dictId = DictionaryId.generate();
        var appleId = WordInfoId.generate();
        var findWords = new StubFindWords(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));

        var generator = new RecordingGenerator(exerciseDto(List.of()));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Animals", dictId,
                "A2", Set.of());
        useCase.execute(command);

        assertThat(generator.receivedWords).isEmpty();
        assertThat(repository.saved.getWordInfoIds()).isEmpty();
        assertThat(increment.incrementedIds).isEmpty();
    }

    @Test
    void shouldIncrementWordsUsedInSentenceOrOptions() {
        var dictId = DictionaryId.generate();
        var travelId = WordInfoId.generate();
        var runId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(travelId, "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                        WordStatus.IN_PROGRESS),
                new WordProjection(runId, "run", Set.of("бежать"), PartOfSpeech.VERB, WordStatus.IN_PROGRESS)));

        var generator = new RecordingGenerator(exerciseDto(List.of(
                new ExerciseItem("I travel every summer.", List.of(), "travel"),
                new ExerciseItem("Choose the verb.", List.of("run", "walk", "jump", "fly"), "run"))));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Travel", dictId,
                "A2", Set.of(travelId, runId));
        useCase.execute(command);

        assertThat(increment.incrementedIds).containsExactlyInAnyOrder(travelId, runId);
    }

    @Test
    void shouldIncrementWordOnlyOnceWhenItAppearsMultipleTimes() {
        var dictId = DictionaryId.generate();
        var travelId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(travelId, "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                        WordStatus.IN_PROGRESS)));

        var generator = new RecordingGenerator(exerciseDto(List.of(
                new ExerciseItem("I travel a lot.", List.of(), "travel"),
                new ExerciseItem("Do you travel often?", List.of("travel", "stay", "leave", "go"), "travel"))));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Present Simple", "Travel", dictId,
                "A2", Set.of(travelId));
        useCase.execute(command);

        assertThat(increment.incrementedIds).containsExactly(travelId);
    }

    @Test
    void shouldMatchWholeWordCaseInsensitively() {
        var dictId = DictionaryId.generate();
        var appleId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(appleId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN, WordStatus.NEW)));

        var generator = new RecordingGenerator(exerciseDto(List.of(
                new ExerciseItem("She ate an Apple.", List.of(), "Apple"))));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Food", dictId,
                "A2", Set.of(appleId));
        useCase.execute(command);

        assertThat(increment.incrementedIds).containsExactly(appleId);
    }

    @Test
    void shouldNotMatchSubstring() {
        var dictId = DictionaryId.generate();
        var catId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(catId, "cat", Set.of("кот"), PartOfSpeech.NOUN, WordStatus.NEW)));

        var generator = new RecordingGenerator(exerciseDto(List.of(
                new ExerciseItem("I found a catalog.", List.of(), "catalog"))));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Reading", dictId,
                "A2", Set.of(catId));
        useCase.execute(command);

        assertThat(increment.incrementedIds).isEmpty();
    }

    @Test
    void shouldNotIncrementWhenItemsAreEmpty() {
        var dictId = DictionaryId.generate();
        var travelId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(travelId, "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                        WordStatus.IN_PROGRESS)));

        var generator = new RecordingGenerator(exerciseDto(List.of()));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Travel", dictId,
                "A2", Set.of(travelId));
        useCase.execute(command);

        assertThat(increment.incrementedIds).isEmpty();
    }

    @Test
    void shouldSkipWordIdMissingFromDictionary() {
        var dictId = DictionaryId.generate();
        var travelId = WordInfoId.generate();
        var missingId = WordInfoId.generate();

        var findWords = new StubFindWords(Set.of(
                new WordProjection(travelId, "travel", Set.of("путешествовать"), PartOfSpeech.VERB,
                        WordStatus.IN_PROGRESS)));

        var generator = new RecordingGenerator(exerciseDto(List.of(
                new ExerciseItem("I travel.", List.of(), "travel"))));
        var repository = new StubExerciseRepository();
        var increment = new RecordingIncrement();
        var useCase = new GenerateExerciseUseCase(repository, generator, findWords, increment);

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Travel", dictId,
                "A2", Set.of(travelId, missingId));
        useCase.execute(command);

        assertThat(generator.receivedWords).extracting(WordData::value).containsExactly("travel");
        assertThat(increment.incrementedIds).containsExactly(travelId);
    }

    private static ExerciseDto exerciseDto(List<ExerciseItem> items) {
        return new ExerciseDto(ExerciseId.generate(), ExerciseType.FILL_IN_THE_BLANK, "Topic", "content",
                List.of("answer"), items, ExerciseStatus.GENERATED);
    }

    static class RecordingGenerator implements ExerciseGenerator {

        Set<WordData> receivedWords;

        private final ExerciseDto dto;

        RecordingGenerator(ExerciseDto dto) {
            this.dto = dto;
        }

        @Override
        public ExerciseDto generate(GenerateExerciseCommand command, Set<WordData> words) {
            this.receivedWords = words;
            return dto;
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
        public Set<WordProjection> findByDictionaryIdAndWordIds(DictionaryId dictionaryId,
                Set<WordInfoId> wordInfoIds) {
            return projections.stream()
                    .filter(projection -> wordInfoIds.contains(projection.id()))
                    .collect(Collectors.toSet());
        }

        @Override
        public DictionaryStats getStats(DictionaryId dictionaryId) {
            return new DictionaryStats(projections.size(), 0, 0, 0);
        }
    }

    static class RecordingIncrement implements IncrementWordRepetitions {

        Set<WordInfoId> incrementedIds;

        @Override
        public void execute(DictionaryId dictionaryId, Set<WordInfoId> wordInfoIds) {
            this.incrementedIds = wordInfoIds;
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
