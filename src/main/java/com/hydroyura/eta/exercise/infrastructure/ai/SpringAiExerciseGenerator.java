package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.config.properties.ExerciseGenerationProperties;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
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

    // language=json
    private static final String FILL_IN_BLANK_SYSTEM = """
            You are an English tutor creating a FILL_IN_THE_BLANK exercise for a student at CEFR level {level}.

            Generate exactly {count} sentences. Each sentence tests the given grammar rule.

            Rules:
            - Exactly ONE blank per sentence, written as ___.
            - The blank must test the specified grammar rule; the learner must apply the rule to determine the answer.
            - The grammar form must be essential: do NOT create a sentence where the answer can be guessed from context alone without applying the rule.
            - Each sentence has exactly one unambiguous correct answer (grammatically and semantically).
            - Use the target vocabulary to build natural context. Target words are NOT the missing words and do NOT determine the answer.
            - All sentences relate to the given general topic.

            Respond with ONLY a valid JSON object, no markdown, no commentary:
            {"items":[{"sentence":"... ___ ...","options":[],"correctAnswer":"travelled"}]}

            For FILL_IN_THE_BLANK, "options" must always be an empty array; "correctAnswer" is the exact word/phrase that fills the blank.
            """;

    // language=json
    private static final String MULTIPLE_CHOICE_SYSTEM = """
            You are an English tutor creating a MULTIPLE_CHOICE exercise for a student at CEFR level {level}.

            Generate exactly {count} questions. Each question tests the given grammar rule.

            Rules:
            - Exactly ONE blank per sentence, written as ___.
            - The blank must test the specified grammar rule; the learner must apply the rule to choose the answer.
            - The grammar form must be essential: do NOT create a question where the correct option can be guessed from context alone without applying the rule.
            - Exactly FOUR options; exactly one is correct grammatically and semantically; the other three are incorrect.
            - The three incorrect options must be plausible grammar mistakes typical for learners (same part of speech), not words that can be eliminated by meaning alone.
            - Correct answers must be balanced across the four positions, not always the same one.
            - Use the target vocabulary to build natural context. Target words are NOT necessarily the options and do NOT determine the answer.
            - All questions relate to the given general topic.

            Respond with ONLY a valid JSON object, no markdown, no commentary:
            {"items":[{"sentence":"... ___ ...","options":["travel","travelled","travelling","travels"],"correctAnswer":"travelled"}]}

            "options" is an array of exactly 4 strings. "correctAnswer" must be the exact text of the correct option (one of the 4 strings).
            """;

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
        var systemPrompt = switch (command.type()) {
            case FILL_IN_THE_BLANK -> FILL_IN_BLANK_SYSTEM
                    .replace("{count}", String.valueOf(count))
                    .replace("{level}", DEFAULT_CEFR_LEVEL);
            case MULTIPLE_CHOICE -> MULTIPLE_CHOICE_SYSTEM
                    .replace("{count}", String.valueOf(count))
                    .replace("{level}", DEFAULT_CEFR_LEVEL);
        };

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

        log.info("Generated exercise: type={}, items={}", command.type(), response.items().size());
        return new ExerciseDto(
                ExerciseId.generate(),
                command.type(),
                command.topic(),
                content,
                expectedAnswers,
                ExerciseStatus.GENERATED
        );
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
                var letters = new char[] { 'A', 'B', 'C', 'D' };
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
                ExerciseStatus.GENERATED
        );
    }
}
