package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.config.properties.ExerciseGenerationProperties;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiExerciseGenerator implements ExerciseGenerator {

    private final ChatClient chatClient;

    private final ExerciseGenerationProperties properties;

    // TODO (техдолг): уровень CEFR должен стать входным параметром, а не хардкодом.
    private static final String DEFAULT_CEFR_LEVEL = "A2";

    private static final String PROMPTS_PATH = "/exercise/prompts/";

    private static final String FILL_IN_BLANK_PROMPT = "fill-in-blank-system.txt";

    private static final String MULTIPLE_CHOICE_PROMPT = "multiple-choice-system.txt";

    @Override
    public ExerciseDto generate(GenerateExerciseCommand command, Set<WordData> words) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(words, "words must not be null");

        var wordList = words.stream()
                .map(w -> "  - " + w.value() + " (" + w.partOfSpeech() + "): " + String.join(", ", w.translations()))
                .collect(Collectors.joining("\n"));

        var userMessage = "Grammar rule: " + command.grammarRule()
                + "\nGeneral topic: " + command.topic()
                + "\n\nTarget vocabulary (context only, NOT the answer):\n" + wordList;

        var count = properties.getSentenceCount();
        var systemPrompt = selectSystemPrompt(command.type())
                .replace("{count}", String.valueOf(count))
                .replace("{level}", DEFAULT_CEFR_LEVEL);

        log.info("Generating {} exercise on grammar '{}', topic '{}' with {} words",
                command.type(), command.grammarRule(), command.topic(), words.size());

        AiExerciseResponse response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .entity(AiExerciseResponse.class);
        } catch (Exception e) {
            log.error("AI exercise generation failed for type={}, topic={}", command.type(), command.topic(), e);
            return fallbackExercise(command, words);
        }

        if (!isValid(response, command.type())) {
            log.warn("AI returned null/invalid response for type={}, topic={}", command.type(), command.topic());
            return fallbackExercise(command, words);
        }

        var content = buildContent(response.items());
        var expectedAnswers = response.items().stream()
                .map(AiExerciseItem::correctAnswer)
                .toList();
        var items = response.items().stream()
                .map(item -> new ExerciseItem(item.sentence(), item.options(), item.correctAnswer()))
                .toList();

        log.info("Generated exercise: type={}, items={}", command.type(), response.items().size());
        return new ExerciseDto(
                ExerciseId.generate(),
                command.type(),
                command.topic(),
                content,
                expectedAnswers,
                items,
                ExerciseStatus.GENERATED
        );
    }

    private String selectSystemPrompt(ExerciseType type) {
        return switch (type) {
            case FILL_IN_THE_BLANK -> loadPrompt(FILL_IN_BLANK_PROMPT);
            case MULTIPLE_CHOICE -> loadPrompt(MULTIPLE_CHOICE_PROMPT);
        };
    }

    private static String loadPrompt(String name) {
        var path = PROMPTS_PATH + name;
        try (var in = SpringAiExerciseGenerator.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Prompt resource not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt: " + path, e);
        }
    }

    private boolean isValid(AiExerciseResponse response, ExerciseType type) {
        if (response == null || response.items() == null || response.items().isEmpty()) {
            return false;
        }
        return response.items().stream()
                .allMatch(item -> item != null
                        && item.sentence() != null && !item.sentence().isBlank()
                        && item.correctAnswer() != null && !item.correctAnswer().isBlank()
                        && optionsValid(item, type));
    }

    private boolean optionsValid(AiExerciseItem item, ExerciseType type) {
        if (type == ExerciseType.FILL_IN_THE_BLANK) {
            return item.options() == null || item.options().isEmpty();
        }
        return item.options() != null
                && item.options().size() == 4
                && item.options().stream().allMatch(o -> o != null && !o.isBlank())
                && item.options().contains(item.correctAnswer());
    }

    private String buildContent(List<AiExerciseItem> items) {
        var sb = new StringBuilder();
        for (var i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(i + 1).append(". ").append(item.sentence());
            if (item.options() != null && !item.options().isEmpty()) {
                var letters = new char[]{'A',
                        'B',
                        'C',
                        'D'};
                for (var j = 0; j < item.options().size(); j++) {
                    sb.append("\n").append(letters[j]).append(") ").append(item.options().get(j));
                }
            }
        }
        return sb.toString();
    }

    private ExerciseDto fallbackExercise(GenerateExerciseCommand command, Set<WordData> words) {
        var wordList = words.stream()
                .map(w -> w.value() + " [" + String.join(", ", w.translations()) + "]")
                .collect(Collectors.joining("\n  "));

        var placeholder = """
                Exercise: %s
                Grammar rule: %s
                Topic: %s
                Words used:
                  %s

                (AI service unavailable — using fallback)
                """.formatted(command.type(), command.grammarRule(), command.topic(), wordList);

        return new ExerciseDto(
                ExerciseId.generate(),
                command.type(),
                command.topic(),
                placeholder,
                List.of("(fallback)"),
                List.of(),
                ExerciseStatus.GENERATED
        );
    }
}
