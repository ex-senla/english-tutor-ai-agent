package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
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

    // language=json
    private static final String FILL_IN_BLANK_SYSTEM = """
        You are English tutor creating hometask for your student.
        Task type is FILL_IN_THE_BLANK exercise.
        Rules:
        - create 10 sentences, where each sentence has only ONE word missing (replaced with ___)
        - every missed word must be related with grammar topic, that provided by user
        - try to use the words provided by the user


        Respond with ONLY a valid JSON object, no markdown, no other text:

        {"content":"1. Sentence with ___ here\\n2. Another ___ sentence\\n...","expectedAnswer":"word1, word2, word3, word4, word5"}
        """;

    // language=json
    private static final String MULTIPLE_CHOICE_SYSTEM = """
        You are an English tutor creating exercises for students.
        Generate a MULTIPLE_CHOICE exercise.

        Rules:
        - Create 3 multiple-choice questions
        - Each question tests understanding of one word from the vocabulary list
        - Use each selected word exactly once
        - Each question has 4 options (A, B, C, D) with only one correct
        - Questions must be meaningful and related to the given topic

        Respond with ONLY a valid JSON object, no markdown, no other text:

        {"content":"1. Question text?\\nA) ... B) ... C) ... D) ...\\n\\n2. ...","expectedAnswer":"A, C, B"}
        """;

    @Override
    public ExerciseDto generate(GenerateExerciseCommand command, Set<WordData> words) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(words, "words must not be null");

        var wordList = words.stream()
            .map(w -> "  - " + w.value() + " (" + w.partOfSpeech() + "): " + String.join(", ", w.translations()))
            .collect(Collectors.joining("\n"));

        var userMessage = "Topic: " + command.topic() + "\n\nVocabulary words:\n" + wordList;

        var systemPrompt = switch (command.type()) {
            case FILL_IN_THE_BLANK -> FILL_IN_BLANK_SYSTEM;
            case MULTIPLE_CHOICE -> MULTIPLE_CHOICE_SYSTEM;
            case MATCHING, TRANSLATION -> throw new UnsupportedOperationException(
                "Exercise type " + command.type() + " is not yet supported");
        };

        log.info("Generating {} exercise on topic '{}' with {} words",
            command.type(), command.topic(), words.size());

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

        if (response == null || response.content() == null) {
            log.warn("AI returned null/empty response for type={}, topic={}", command.type(), command.topic());
            return fallbackExercise(command, words);
        }

        log.info("Generated exercise: type={}, contentLength={}", command.type(), response.content().length());
        return new ExerciseDto(
            ExerciseId.generate(),
            command.type(),
            command.topic(),
            response.content(),
            response.expectedAnswer(),
            ExerciseStatus.GENERATED
        );
    }

    private ExerciseDto fallbackExercise(GenerateExerciseCommand command, Set<WordData> words) {
        var wordList = words.stream()
            .map(w -> w.value() + " [" + String.join(", ", w.translations()) + "]")
            .collect(Collectors.joining("\n  "));

        var placeholder = """
            Exercise: %s
            Topic: %s
            Words used:
              %s

            (AI service unavailable — using fallback)
            """.formatted(command.type(), command.topic(), wordList);

        return new ExerciseDto(
            ExerciseId.generate(),
            command.type(),
            command.topic(),
            placeholder,
            "(fallback)",
            ExerciseStatus.GENERATED
        );
    }
}
