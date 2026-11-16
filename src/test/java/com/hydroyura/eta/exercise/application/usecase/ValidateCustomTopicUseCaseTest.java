package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.exercise.api.exercise.ValidateCustomTopicCommand;
import com.hydroyura.eta.exercise.application.port.CustomTopicValidator;
import com.hydroyura.eta.exercise.application.port.TopicValidationVerdict;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidateCustomTopicUseCaseTest {

    private CustomTopicValidator validator;

    private ValidateCustomTopicUseCase useCase;

    @BeforeEach
    void setUp() {
        validator = mock(CustomTopicValidator.class);
        useCase = new ValidateCustomTopicUseCase(validator,
                new GrammarTopicCatalog("/exercise/topics/test-grammar-topics.json"));
    }

    @Test
    void shouldRejectEmptyInputWithoutCallingLlm() {
        var result = useCase.validate(new ValidateCustomTopicCommand("   "));

        assertThat(result.validated()).isTrue();
        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isNotBlank();
        verify(validator, never()).validate(anyString());
    }

    @Test
    void shouldRejectTooLongInputWithoutCallingLlm() {
        var result = useCase.validate(new ValidateCustomTopicCommand("a".repeat(101)));

        assertThat(result.validated()).isTrue();
        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).contains("100");
        verify(validator, never()).validate(anyString());
    }

    @Test
    void shouldAcceptValidVerdictAndCanonicalName() {
        when(validator.validate(anyString())).thenReturn(Optional.of(
                new TopicValidationVerdict(true, "Past Simple", "past-simple", null)));

        var result = useCase.validate(new ValidateCustomTopicCommand("past simple"));

        assertThat(result.validated()).isTrue();
        assertThat(result.valid()).isTrue();
        assertThat(result.canonicalName()).isEqualTo("Past Simple");
        assertThat(result.matchedSlug()).isEqualTo("past-simple");
    }

    @Test
    void shouldNullifyHallucinatedMatchedSlug() {
        when(validator.validate(anyString())).thenReturn(Optional.of(
                new TopicValidationVerdict(true, "Past Simple", "ghost-slug", null)));

        var result = useCase.validate(new ValidateCustomTopicCommand("past simple"));

        assertThat(result.validated()).isTrue();
        assertThat(result.matchedSlug()).isNull();
    }

    @Test
    void shouldFailOpenWhenValidatorThrows() {
        when(validator.validate(anyString())).thenThrow(new RuntimeException("AI down"));

        var result = useCase.validate(new ValidateCustomTopicCommand("  Past Simple  "));

        assertThat(result.validated()).isFalse();
        assertThat(result.valid()).isTrue();
        assertThat(result.canonicalName()).isEqualTo("Past Simple");
        assertThat(result.matchedSlug()).isNull();
    }

    @Test
    void shouldFailOpenWhenValidatorReturnsEmpty() {
        when(validator.validate(anyString())).thenReturn(Optional.empty());

        var result = useCase.validate(new ValidateCustomTopicCommand("Past Simple"));

        assertThat(result.validated()).isFalse();
        assertThat(result.canonicalName()).isEqualTo("Past Simple");
    }
}
