package com.hydroyura.eta.dictionary.api.word;

import java.util.List;

/**
 * Результат one-shot определения части речи и переводов слова/выражения.
 *
 * @param partOfSpeech автоопределённая часть речи
 * @param translations валидированные переводы (может быть пустым при сбое)
 */
public record WordPosTranslations(PartOfSpeech partOfSpeech, List<String> translations) {

    public WordPosTranslations {
        translations = translations == null ? List.of() : List.copyOf(translations);
    }
}
