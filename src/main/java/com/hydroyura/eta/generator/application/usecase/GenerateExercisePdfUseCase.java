package com.hydroyura.eta.generator.application.usecase;

import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.generator.api.document.ExercisePdfBundle;
import com.hydroyura.eta.generator.api.document.ExercisePdfDocument;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdf;
import com.hydroyura.eta.generator.api.document.GenerateExercisePdfCommand;
import com.hydroyura.eta.generator.application.view.AnswerItemView;
import com.hydroyura.eta.generator.application.view.ExerciseItemView;
import com.hydroyura.eta.generator.application.view.UsedWordView;
import com.hydroyura.eta.generator.domain.document.ExercisePdfRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenerateExercisePdfUseCase implements GenerateExercisePdf {

    private final ExercisePdfRenderer renderer;

    public GenerateExercisePdfUseCase(ExercisePdfRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    @Override
    public ExercisePdfBundle execute(GenerateExercisePdfCommand command) {
        var exercise = command.exercise();
        var vocabulary = command.vocabularyWords();

        var usedWords = determineUsedWords(exercise, vocabulary);

        var exerciseDoc = renderExercise(exercise, usedWords);
        var answersDoc = renderAnswers(exercise, usedWords);

        return new ExercisePdfBundle(exerciseDoc, answersDoc);
    }

    private List<WordProjection> determineUsedWords(ExerciseDto exercise, List<WordProjection> vocabulary) {
        var combined = new StringBuilder();
        for (var item : exercise.items()) {
            combined.append(item.sentence()).append(' ');
            item.options().forEach(option -> combined.append(option).append(' '));
        }

        return vocabulary.stream()
                .filter(word -> containsWord(combined.toString(), word.value()))
                .sorted(Comparator.comparing(word -> word.value().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private ExercisePdfDocument renderExercise(ExerciseDto exercise, List<WordProjection> usedWords) {
        var values = usedWords.stream().map(WordProjection::value).toList();

        var items = new ArrayList<ExerciseItemView>();
        for (var item : exercise.items()) {
            var sentence = bold(escapeHtml(item.sentence()), values);

            var options = new ArrayList<String>();
            for (var j = 0; j < item.options().size(); j++) {
                var letter = (char) ('A' + j);
                options.add(letter + ") " + bold(escapeHtml(item.options().get(j)), values));
            }

            items.add(new ExerciseItemView(sentence, options));
        }

        Map<String, Object> model = Map.of(
                "type", exercise.type().name(),
                "topic", exercise.topic(),
                "items", items
        );

        var bytes = renderer.render("exercise", model);
        return new ExercisePdfDocument(fileName("exercise", exercise), bytes);
    }

    private ExercisePdfDocument renderAnswers(ExerciseDto exercise, List<WordProjection> usedWords) {
        var answers = new ArrayList<AnswerItemView>();
        for (var i = 0; i < exercise.items().size(); i++) {
            var item = exercise.items().get(i);
            answers.add(new AnswerItemView(i + 1, escapeHtml(item.correctAnswer())));
        }

        var used = usedWords.stream()
                .map(word -> new UsedWordView(escapeHtml(word.value()),
                        escapeHtml(String.join(", ", word.translations()))))
                .toList();

        Map<String, Object> model = Map.of(
                "type", exercise.type().name(),
                "topic", exercise.topic(),
                "answers", answers,
                "usedWords", used
        );

        var bytes = renderer.render("answers", model);
        return new ExercisePdfDocument(fileName("answers", exercise), bytes);
    }

    private static String fileName(String kind, ExerciseDto exercise) {
        var topic = sanitize(exercise.topic());
        if (topic.isBlank()) {
            topic = "exercise";
        }
        return kind + "_" + topic + ".pdf";
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "_");
    }

    private static boolean containsWord(String text, String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        var pattern = Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b");
        return pattern.matcher(text).find();
    }

    private static String bold(String text, List<String> words) {
        if (text == null || words.isEmpty()) {
            return text;
        }
        var sorted = words.stream()
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
        var alternation = sorted.stream().map(Pattern::quote).collect(Collectors.joining("|"));
        var pattern = Pattern.compile("(?i)\\b(?:" + alternation + ")\\b");
        return pattern.matcher(text).replaceAll("<b>$0</b>");
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
