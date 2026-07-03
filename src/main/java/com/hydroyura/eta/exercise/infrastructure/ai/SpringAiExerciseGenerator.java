package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Skeleton adapter for Spring AI.
 * Will be replaced with actual AI call when Spring AI is integrated.
 */
@Component
public class SpringAiExerciseGenerator implements ExerciseGenerator {

    @Override
    public ExerciseDto generate(GenerateExerciseCommand command, Set<WordData> words) {
        var wordList = words.stream()
            .map(w -> w.value() + " [" + String.join(", ", w.translations()) + "]")
            .collect(Collectors.joining("\n  "));

        var placeholder = """
            Exercise: %s
            Topic: %s
            Words used:
              %s
            
            (AI-generated content will appear here)
            """.formatted(command.type(), command.topic(), wordList);

        var expectedAnswer = switch (command.type()) {
            case FILL_IN_THE_BLANK -> "(expected word)";
            case TRANSLATION -> "(expected translation)";
            case MATCHING -> "(expected matches)";
            case MULTIPLE_CHOICE -> "A";
        };

        return new ExerciseDto(
            ExerciseId.generate(),
            command.type(),
            command.topic(),
            placeholder,
            expectedAnswer,
            ExerciseStatus.GENERATED
        );
    }
}
