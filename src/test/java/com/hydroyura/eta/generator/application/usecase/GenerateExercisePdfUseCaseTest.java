package com.hydroyura.eta.generator.application.usecase;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdfCommand;
import com.hydroyura.eta.generator.application.view.ExerciseItemView;
import com.hydroyura.eta.generator.application.view.UsedWordView;
import com.hydroyura.eta.generator.domain.document.ExercisePdfRenderer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateExercisePdfUseCaseTest {

    private RecordingRenderer renderer;

    private GenerateExercisePdfUseCase useCase;

    @BeforeEach
    void setUp() {
        renderer = new RecordingRenderer();
        useCase = new GenerateExercisePdfUseCase(renderer);
    }

    @Test
    void shouldProduceTwoPdfDocumentsWithDistinctNames() {
        var bundle = useCase.execute(command(exercise("I travel to the mountains ___"),
                List.of(word("travel", "путешествовать"), word("apple", "яблоко"))));

        assertThat(bundle.exercise().fileName()).startsWith("exercise_");
        assertThat(bundle.answers().fileName()).startsWith("answers_");
        assertThat(bundle.exercise().fileName()).isNotEqualTo(bundle.answers().fileName());
        assertThat(new String(bundle.exercise().content(), StandardCharsets.UTF_8)).startsWith("%PDF");
        assertThat(new String(bundle.answers().content(), StandardCharsets.UTF_8)).startsWith("%PDF");
    }

    @Test
    void shouldBoldUsedWordsInStudentDocument() {
        useCase.execute(command(exercise("I travel to the mountains ___"),
                List.of(word("travel", "путешествовать"), word("apple", "яблоко"))));

        var model = renderer.modelFor("exercise");
        var items = (List<ExerciseItemView>) model.get("items");

        assertThat(items.get(0).getSentence()).contains("<b>travel</b>");
        assertThat(items.get(0).getSentence()).doesNotContain("<b>apple</b>");
    }

    @Test
    void shouldListOnlyUsedWordsInTeacherDocument() {
        useCase.execute(command(exercise("I travel to the mountains ___"),
                List.of(word("travel", "путешествовать"), word("apple", "яблоко"))));

        var model = renderer.modelFor("answers");
        var usedWords = (List<UsedWordView>) model.get("usedWords");

        assertThat(usedWords).hasSize(1);
        assertThat(usedWords.get(0).getValue()).isEqualTo("travel");
        assertThat(usedWords.get(0).getTranslations()).isEqualTo("путешествовать");
    }

    @Test
    void shouldMatchVocabularyCaseInsensitive() {
        useCase.execute(command(exercise("We Travel home ___"), List.of(word("travel", "путешествовать"))));

        var model = renderer.modelFor("exercise");
        var items = (List<ExerciseItemView>) model.get("items");

        assertThat(items.get(0).getSentence()).contains("<b>Travel</b>");
    }

    @Test
    void shouldNotMatchWordInsideAnotherWord() {
        useCase.execute(command(exercise("We travelled home ___"), List.of(word("travel", "путешествовать"))));

        var model = renderer.modelFor("exercise");
        var items = (List<ExerciseItemView>) model.get("items");

        assertThat(items.get(0).getSentence()).doesNotContain("<b>");
    }

    private ExerciseDto exercise(String sentence) {
        return new ExerciseDto(
                ExerciseId.generate(),
                ExerciseType.FILL_IN_THE_BLANK,
                "Animals",
                "1. " + sentence,
                List.of("travelled"),
                List.of(new ExerciseItem(sentence, List.of(), "travelled")),
                ExerciseStatus.GENERATED
        );
    }

    private WordProjection word(String value, String translation) {
        return new WordProjection(WordId.generate(), value, java.util.Set.of(translation), PartOfSpeech.VERB,
                WordStatus.IN_PROGRESS);
    }

    private GenerateExercisePdfCommand command(ExerciseDto exercise, List<WordProjection> vocabulary) {
        return new GenerateExercisePdfCommand(exercise, vocabulary);
    }

    static class RecordingRenderer implements ExercisePdfRenderer {

        private final List<String> templates = new ArrayList<>();

        private final List<Map<String, Object>> models = new ArrayList<>();

        @Override
        public byte[] render(String templateName, Map<String, Object> model) {
            templates.add(templateName);
            models.add(model);
            return ("%PDF-1.4 " + templateName).getBytes(StandardCharsets.UTF_8);
        }

        Map<String, Object> modelFor(String template) {
            return models.get(templates.indexOf(template));
        }
    }
}
