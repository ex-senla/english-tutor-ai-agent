package com.hydroyura.eta.exercise.application.port;

import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import java.util.Set;

public interface ExerciseGenerator {

    ExerciseDto generate(GenerateExerciseCommand command, Set<WordData> words);
}
