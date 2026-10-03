package com.hydroyura.eta.exercise.infrastructure.ai;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.config.properties.ExerciseGenerationProperties;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringAiExerciseGeneratorTest {

    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec callResponseSpec;

    private SpringAiExerciseGenerator generator;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        generator = new SpringAiExerciseGenerator(chatClient, new ExerciseGenerationProperties());
    }

    @Test
    void shouldBuildStructuredExerciseFromResponse() {
        stubResponse(new AiExerciseResponse(List.of(
                new AiExerciseItem("Last summer, I ___ to the mountains.", List.of(), "travelled"))));

        var dto = generator.generate(command(), words());

        assertThat(dto.type()).isEqualTo(ExerciseType.FILL_IN_THE_BLANK);
        assertThat(dto.content()).contains("1. Last summer, I ___ to the mountains.");
        assertThat(dto.expectedAnswers()).containsExactly("travelled");
        assertThat(dto.items()).containsExactly(
                new ExerciseItem("Last summer, I ___ to the mountains.", List.of(), "travelled"));
        assertThat(dto.status()).isEqualTo(ExerciseStatus.GENERATED);
    }

    @Test
    void shouldPopulateItemsForMultipleChoice() {
        stubResponse(new AiExerciseResponse(List.of(
                new AiExerciseItem("I ___ home yesterday.", List.of("go", "went", "gone", "going"), "went"))));

        var dto = generator.generate(multipleChoiceCommand(), words());

        assertThat(dto.items()).hasSize(1);
        var item = dto.items().get(0);
        assertThat(item.sentence()).contains("___");
        assertThat(item.options()).containsExactly("go", "went", "gone", "going");
        assertThat(item.correctAnswer()).isEqualTo("went");
    }

    @Test
    void shouldReturnFallbackWhenChatClientThrows() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("AI down"));

        var dto = generator.generate(command(), words());

        assertThat(dto.content()).contains("(AI service unavailable — using fallback)");
        assertThat(dto.expectedAnswers()).containsExactly("(fallback)");
    }

    @Test
    void shouldReturnFallbackWhenResponseInvalid() {
        stubResponse(new AiExerciseResponse(List.of(new AiExerciseItem("   ", List.of(), ""))));

        var dto = generator.generate(command(), words());

        assertThat(dto.content()).contains("(AI service unavailable — using fallback)");
    }

    private void stubResponse(AiExerciseResponse response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(AiExerciseResponse.class)).thenReturn(response);
    }

    @Test
    void shouldUseCefrLevelFromCommandInSystemPrompt() {
        stubResponse(new AiExerciseResponse(List.of(
                new AiExerciseItem("Last summer, I ___ to the mountains.", List.of(), "travelled"))));

        generator.generate(command(), words());

        var captor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).system(captor.capture());
        assertThat(captor.getValue()).contains("level B1");
    }

    private GenerateExerciseCommand command() {
        return new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK, "Past Simple", "Animals", DictionaryId
                .generate(), "B1");
    }

    private GenerateExerciseCommand multipleChoiceCommand() {
        return new GenerateExerciseCommand(ExerciseType.MULTIPLE_CHOICE, "Past Simple", "Animals", DictionaryId
                .generate(), "B1");
    }

    private Set<WordData> words() {
        return Set.of(new WordData("travel", Set.of("путешествовать"), "VERB"));
    }
}
