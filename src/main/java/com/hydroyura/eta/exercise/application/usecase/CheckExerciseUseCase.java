package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseCommand;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseResult;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CheckExerciseUseCase implements CheckExercise {

    private final ExerciseRepository repository;

    public CheckExerciseUseCase(ExerciseRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public CheckExerciseResult execute(CheckExerciseCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        var exercise = repository.findById(command.exerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + command.exerciseId()));

        exercise.markAnswered();

        var expectedAnswers = exercise.getExpectedAnswers();
        var userAnswers = Arrays.stream(command.userAnswer().split(","))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();

        var feedbackLines = new ArrayList<String>();
        var correct = userAnswers.size() == expectedAnswers.size();
        for (var i = 0; i < expectedAnswers.size(); i++) {
            var itemCorrect = i < userAnswers.size()
                    && normalize(userAnswers.get(i)).equals(normalize(expectedAnswers.get(i)));
            if (!itemCorrect) {
                correct = false;
            }
            feedbackLines.add((i + 1) + ") " + (itemCorrect ? "✅" : "❌ (ожидалось: " + expectedAnswers.get(i) + ")"));
        }

        if (correct) {
            exercise.markChecked();
        }

        repository.save(exercise);

        var dto = new ExerciseDto(
                exercise.getId(),
                exercise.getType(),
                exercise.getTopic(),
                exercise.getContent(),
                exercise.getExpectedAnswers(),
                exercise.getStatus()
        );

        return new CheckExerciseResult(correct, String.join("\n", feedbackLines), dto);
    }

    private String normalize(String s) {
        return s.strip().toLowerCase().replaceAll("\\s+", " ");
    }
}
