package com.hydroyura.eta.exercise.api.exercise;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import java.util.Objects;
import java.util.Set;

/**
 * Команда генерации упражнения.
 *
 * @param topic         тема упражнения — предустановленная тема каталога (по slug) или «своя тема» (текст).
 * @param thematicTopic опциональная тематическая тема (предметно-контекстный слой, например «Travel»);
 *                      {@code null}/blank означает «не задана».
 * @param cefrLevel     код уровня CEFR (A1–C2); валидность значения гарантируется источником
 *                      (enum {@code CefrLevel} ученика в модуле student), поэтому здесь проверяется только непустота.
 * @param wordIds       идентификаторы слов словаря, выбранных для упражнения; может быть пустым.
 */
public record GenerateExerciseCommand(
        ExerciseType type,
        ExerciseTopic topic,
        String thematicTopic,
        DictionaryId dictionaryId,
        String cefrLevel,
        Set<WordInfoId> wordIds
) {

    public GenerateExerciseCommand {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(dictionaryId, "dictionaryId must not be null");
        Objects.requireNonNull(cefrLevel, "cefrLevel must not be null");
        if (cefrLevel.isBlank()) {
            throw new IllegalArgumentException("cefrLevel must not be blank");
        }
        Objects.requireNonNull(wordIds, "wordIds must not be null");
        wordIds = Set.copyOf(wordIds);
    }
}
