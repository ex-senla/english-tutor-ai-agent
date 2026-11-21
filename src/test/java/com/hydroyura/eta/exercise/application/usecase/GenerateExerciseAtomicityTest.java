package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.IncrementWordRepetitions;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class GenerateExerciseAtomicityTest extends PostgresIntegrationTestBase {

    @Autowired
    private GenerateExercise generateExercise;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private FindWords findWords;

    @MockitoBean
    private ExerciseGenerator exerciseGenerator;

    @MockitoBean
    private IncrementWordRepetitions incrementWordRepetitions;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM exercise_word_info");
        jdbcTemplate.execute("DELETE FROM exercise");
    }

    @Test
    void shouldNotPersistExerciseWhenIncrementFails() {
        when(findWords.findByDictionaryIdAndWordIds(any(), any())).thenReturn(Set.of());
        when(exerciseGenerator.generate(any(), any())).thenReturn(new ExerciseDto(
                ExerciseId.generate(), ExerciseType.FILL_IN_THE_BLANK, "Topic", null, "content",
                List.of("answer"), List.of(), ExerciseStatus.GENERATED));
        doThrow(new RuntimeException("boom")).when(incrementWordRepetitions).execute(any(), any());

        var command = new GenerateExerciseCommand(ExerciseType.FILL_IN_THE_BLANK,
                new CatalogTopic("present-simple"), null, DictionaryId.generate(), "A2", Set.of());

        assertThatThrownBy(() -> generateExercise.execute(command)).isInstanceOf(RuntimeException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM exercise", Integer.class)).isZero();
    }
}
