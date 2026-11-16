package com.hydroyura.eta.dictionary.api.word;

import java.util.List;

/**
 * Предлагает варианты перевода английского слова с учётом части речи.
 * Количество вариантов не входит в контракт — им владеет адаптер
 * ({@code dictionary.application.config.properties.WordTranslationProperties}).
 */
public interface WordTranslator {

    List<String> translate(WordTranslationCommand command);

    /**
     * One-shot определение части речи и переводов по слову/выражению без явного выбора POS.
     *
     * @param word валидированное значение слова
     * @return часть речи и переводы; при сбое/неизвестном POS — {@code partOfSpeech == null}.
     */
    WordPosTranslations suggestPosAndTranslations(String word);
}
