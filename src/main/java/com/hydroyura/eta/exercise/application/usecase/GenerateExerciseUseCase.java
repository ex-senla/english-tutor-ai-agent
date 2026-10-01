package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenerateExerciseUseCase implements GenerateExercise {

    private final ExerciseRepository repository;

    private final ExerciseGenerator generator;

    private final FindWords findWords;

    public GenerateExerciseUseCase(ExerciseRepository repository, ExerciseGenerator generator, FindWords findWords) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.generator = Objects.requireNonNull(generator, "generator must not be null");
        this.findWords = Objects.requireNonNull(findWords, "findWords must not be null");
    }

    @Override
    public ExerciseDto execute(GenerateExerciseCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // TODO (временно): берём все слова словаря без фильтра по статусу IN_PROGRESS.
        // Вернуть фильтр по статусу IN_PROGRESS (roadmap: «слова в статусе на изучении»).
        var words = findWords.findByDictionaryId(command.dictionaryId());

        if (words.isEmpty()) {
            throw new IllegalArgumentException(
                    "No words in dictionary: " + command.dictionaryId().value());
        }

        var wordIds = words.stream()
                .map(WordProjection::id)
                .collect(Collectors.toSet());

        var exercise = Exercise.create(ExerciseId.generate(), command.type(), command.topic(), wordIds);

        var wordDataList = words.stream()
                .map(wp -> new WordData(wp.value(), wp.translations(), wp.partOfSpeech().name()))
                .collect(Collectors.toSet());

        var dto = generator.generate(command, wordDataList);
        exercise.setContent(dto.content());
        exercise.setExpectedAnswers(dto.expectedAnswers());

        repository.save(exercise);

        return new ExerciseDto(exercise.getId(), exercise.getType(), exercise.getTopic(),
                exercise.getContent(), exercise.getExpectedAnswers(), exercise.getStatus());
    }
}
