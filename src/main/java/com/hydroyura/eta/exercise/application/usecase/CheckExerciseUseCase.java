package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.exercise.api.exercise.CheckExercise;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseCommand;
import com.hydroyura.eta.exercise.api.exercise.CheckExerciseResult;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
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

        var expectedAnswer = exercise.getExpectedAnswer();
        var correct = normalize(command.userAnswer()).equals(normalize(expectedAnswer));

        if (correct) {
            exercise.markChecked();
        }

        repository.save(exercise);

        var feedback = correct
            ? "✅ Correct!"
            : "❌ Incorrect. Expected: " + expectedAnswer;

        var dto = new ExerciseDto(
            exercise.getId(),
            exercise.getType(),
            exercise.getTopic(),
            exercise.getContent(),
            exercise.getExpectedAnswer(),
            exercise.getStatus()
        );

        return new CheckExerciseResult(correct, feedback, dto);
    }

    private String normalize(String s) {
        return s.strip().toLowerCase().replaceAll("\\s+", " ");
    }
}
