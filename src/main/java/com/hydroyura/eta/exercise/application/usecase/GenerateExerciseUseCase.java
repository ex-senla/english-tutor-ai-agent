package com.hydroyura.eta.exercise.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.dictionary.IncrementWordRepetitions;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.api.word.WordProjection;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.GenerateExercise;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.application.port.WordData;
import com.hydroyura.eta.exercise.application.topic.GrammarTopicCatalog;
import com.hydroyura.eta.exercise.domain.exercise.Exercise;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseRepository;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public class GenerateExerciseUseCase implements GenerateExercise {

    private final ExerciseRepository repository;

    private final ExerciseGenerator generator;

    private final FindWords findWords;

    private final IncrementWordRepetitions incrementWordRepetitions;

    private final GrammarTopicCatalog topicCatalog;

    private final TransactionTemplate transactionTemplate;

    public GenerateExerciseUseCase(ExerciseRepository repository, ExerciseGenerator generator, FindWords findWords,
            IncrementWordRepetitions incrementWordRepetitions, GrammarTopicCatalog topicCatalog,
            TransactionTemplate transactionTemplate) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.generator = Objects.requireNonNull(generator, "generator must not be null");
        this.findWords = Objects.requireNonNull(findWords, "findWords must not be null");
        this.incrementWordRepetitions = Objects.requireNonNull(incrementWordRepetitions,
                "incrementWordRepetitions must not be null");
        this.topicCatalog = Objects.requireNonNull(topicCatalog, "topicCatalog must not be null");
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate, "transactionTemplate must not be null");
    }

    @Override
    public ExerciseDto execute(GenerateExerciseCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        var words = findWords.findByDictionaryIdAndWordIds(command.dictionaryId(), command.wordIds());

        var displayTopic = topicCatalog.displayTopic(command.topic());
        var exercise = Exercise.create(ExerciseId.generate(), command.type(), displayTopic, command.thematicTopic(),
                command.wordIds());

        var wordDataList = words.stream()
                .map(wp -> new WordData(wp.value(), wp.translations(), wp.partOfSpeech().name()))
                .collect(Collectors.toSet());

        var dto = generator.generate(command, wordDataList);
        exercise.setContent(dto.content());
        exercise.setExpectedAnswers(dto.expectedAnswers());
        exercise.setItems(dto.items());

        var usedIds = determineUsedWordIds(words, dto.items());

        // LLM-вызов выполнен вне транзакции; запись упражнения и инкременты — атомарно в короткой транзакции.
        transactionTemplate.executeWithoutResult(status -> {
            repository.save(exercise);
            incrementWordRepetitions.execute(command.dictionaryId(), usedIds);
        });
        log.info("Incremented repetitions for used words {}", usedIds);

        return new ExerciseDto(exercise.getId(), exercise.getType(), exercise.getTopic(),
                exercise.getThematicTopic(), exercise.getContent(), exercise.getExpectedAnswers(),
                exercise.getItems(), exercise.getStatus());
    }

    private static Set<WordInfoId> determineUsedWordIds(Set<WordProjection> words, List<ExerciseItem> items) {
        var combined = new StringBuilder();
        for (var item : items) {
            combined.append(item.sentence()).append(' ');
            item.options().forEach(option -> combined.append(option).append(' '));
        }

        return words.stream()
                .filter(word -> containsWord(combined.toString(), word.value()))
                .map(WordProjection::id)
                .collect(Collectors.toSet());
    }

    private static boolean containsWord(String text, String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        var pattern = Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b");
        return pattern.matcher(text).find();
    }
}
