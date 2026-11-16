package com.hydroyura.eta.generator.application.usecase;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateExercisePdfUseCaseTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T20:30:00.123Z"), ZoneOffset.UTC);

    private RecordingRenderer renderer;

    private GenerateExercisePdfUseCase useCase;

    @BeforeEach
    void setUp() {
        renderer = new RecordingRenderer();
        useCase = new GenerateExercisePdfUseCase(renderer, FIXED_CLOCK);
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
    void shouldIncludeMetadataInFileName() {
        var bundle = useCase.execute(command(exercise("I travel to the mountains ___"),
                List.of(word("travel", "путешествовать"))));

        assertThat(bundle.exercise().fileName())
                .startsWith("exercise_animals_b1_alice_")
                .contains("20261021-203000-123")
                .endsWith(".pdf");
        assertThat(bundle.answers().fileName())
                .startsWith("answers_animals_b1_alice_");
    }

    @Test
    void shouldProduceUniqueFileNamesAcrossGenerations() {
        var clock = new MutableClock(Instant.parse("2026-10-21T20:30:00.123Z"));
        var localUseCase = new GenerateExercisePdfUseCase(renderer, clock);
        var cmd = command(exercise("I travel to the mountains ___"),
                List.of(word("travel", "путешествовать")));

        var first = localUseCase.execute(cmd);
        clock.set(Instant.parse("2026-10-21T20:30:01.456Z"));
        var second = localUseCase.execute(cmd);

        assertThat(first.exercise().fileName()).isNotEqualTo(second.exercise().fileName());
        assertThat(first.answers().fileName()).isNotEqualTo(second.answers().fileName());
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

    @Test
    void shouldNotBoldDictionaryWordsInOptions() {
        useCase.execute(command(
                exerciseWithOptions("We ___ home.", List.of("travel", "travelled", "travels")),
                List.of(word("travel", "путешествовать"))));

        var model = renderer.modelFor("exercise");
        var items = (List<ExerciseItemView>) model.get("items");

        assertThat(items.get(0).getOptions())
                .allSatisfy(option -> assertThat(option).doesNotContain("<b>"));
    }

    @Test
    void shouldIncludeContextBadgeInBothDocumentsWhenThematicTopicPresent() {
        useCase.execute(command(exerciseWithThematicTopic("I travel ___ daily.", "Travel"),
                List.of(word("travel", "путешествовать"))));

        assertThat(renderer.modelFor("exercise").get("thematicTopic")).isEqualTo("Travel");
        assertThat(renderer.modelFor("answers").get("thematicTopic")).isEqualTo("Travel");
    }

    @Test
    void shouldNotIncludeContextBadgeWhenThematicTopicBlank() {
        useCase.execute(command(exercise("I travel ___ daily."), List.of(word("travel", "путешествовать"))));

        assertThat(renderer.modelFor("exercise")).doesNotContainKey("thematicTopic");
        assertThat(renderer.modelFor("answers")).doesNotContainKey("thematicTopic");
    }

    @Test
    void shouldPutRawThematicTopicInModel() {
        useCase.execute(command(exerciseWithThematicTopic("I travel ___ daily.", "Food & <drinks>"),
                List.of(word("travel", "путешествовать"))));

        // Thymeleaf (th:text) is responsible for escaping; the use case must not pre-escape,
        // otherwise the value would be escaped twice in the rendered PDF.
        assertThat(renderer.modelFor("exercise").get("thematicTopic")).isEqualTo("Food & <drinks>");
        assertThat(renderer.modelFor("answers").get("thematicTopic")).isEqualTo("Food & <drinks>");
    }

    @Test
    void shouldNotIncludeThematicTopicInFileName() {
        var bundle = useCase.execute(command(exerciseWithThematicTopic("I travel ___ daily.", "Travel"),
                List.of(word("travel", "путешествовать"))));

        assertThat(bundle.exercise().fileName()).doesNotContain("travel");
        assertThat(bundle.answers().fileName()).doesNotContain("travel");
        assertThat(bundle.exercise().fileName()).startsWith("exercise_animals_b1_alice_");
    }

    private ExerciseDto exercise(String sentence) {
        return new ExerciseDto(
                ExerciseId.generate(),
                ExerciseType.FILL_IN_THE_BLANK,
                "Animals",
                null,
                "1. " + sentence,
                List.of("travelled"),
                List.of(new ExerciseItem(sentence, List.of(), "travelled")),
                ExerciseStatus.GENERATED
        );
    }

    private ExerciseDto exerciseWithOptions(String sentence, List<String> options) {
        return new ExerciseDto(
                ExerciseId.generate(),
                ExerciseType.MULTIPLE_CHOICE,
                "Travel",
                null,
                "1. " + sentence,
                List.of("travelled"),
                List.of(new ExerciseItem(sentence, options, "travelled")),
                ExerciseStatus.GENERATED
        );
    }

    private ExerciseDto exerciseWithThematicTopic(String sentence, String thematicTopic) {
        return new ExerciseDto(
                ExerciseId.generate(),
                ExerciseType.FILL_IN_THE_BLANK,
                "Animals",
                thematicTopic,
                "1. " + sentence,
                List.of("travelled"),
                List.of(new ExerciseItem(sentence, List.of(), "travelled")),
                ExerciseStatus.GENERATED
        );
    }

    private WordProjection word(String value, String translation) {
        return new WordProjection(WordInfoId.generate(), value, java.util.Set.of(translation), PartOfSpeech.VERB,
                WordStatus.IN_PROGRESS);
    }

    private GenerateExercisePdfCommand command(ExerciseDto exercise, List<WordProjection> vocabulary) {
        return new GenerateExercisePdfCommand(exercise, vocabulary, "Alice", "B1");
    }

    static final class MutableClock extends Clock {

        private final ZoneId zone = ZoneOffset.UTC;

        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void set(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
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
