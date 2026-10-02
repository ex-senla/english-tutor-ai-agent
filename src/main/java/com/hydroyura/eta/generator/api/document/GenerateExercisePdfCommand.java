package com.hydroyura.eta.generator.api.document;

import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import java.util.List;
import java.util.Objects;

public record GenerateExercisePdfCommand(
        ExerciseDto exercise,
        List<WordProjection> vocabularyWords
) {

    public GenerateExercisePdfCommand {
        Objects.requireNonNull(exercise, "exercise must not be null");
        Objects.requireNonNull(vocabularyWords, "vocabularyWords must not be null");
        vocabularyWords = List.copyOf(vocabularyWords);
    }
}
