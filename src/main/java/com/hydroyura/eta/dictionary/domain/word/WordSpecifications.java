package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.shared.api.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class WordSpecifications {

    private final WordSpecificationConfig config;

    private Specification<Word> warnFalse(String name, Specification<Word> spec) {
        return word -> {
            boolean satisfied = spec.isSatisfiedBy(word);
            if (!satisfied) {
                log.warn("Word validation failed: {} for word '{}'", name, word.getValue());
            }
            return satisfied;
        };
    }

    public Specification<Word> valueNotBlank() {
        return warnFalse("valueNotBlank",
                word -> word.getValue() != null && !word.getValue().isBlank());
    }

    public Specification<Word> valueMatchesPattern() {
        return warnFalse("valueMatchesPattern",
                word -> word.getValue() != null
                        && word.getValue().matches(config.valueAllowedPattern()));
    }

    public Specification<Word> valueLengthInRange() {
        return warnFalse("valueLengthInRange",
                word -> word.getValue() != null
                        && word.getValue().length() >= config.valueMinLength()
                        && word.getValue().length() <= config.valueMaxLength());
    }

    public Specification<Word> translationsNotNull() {
        return warnFalse("translationsNotNull",
                word -> word.getTranslations() != null);
    }

    public Specification<Word> translationsNotEmpty() {
        return warnFalse("translationsNotEmpty",
                word -> !word.getTranslations().isEmpty());
    }

    public Specification<Word> hasMinTranslations() {
        return warnFalse("hasMinTranslations",
                word -> word.getTranslations().size() >= config.minTranslations());
    }

    public Specification<Word> translationsMatchPattern() {
        return warnFalse("translationsMatchPattern",
                word -> word.getTranslations().stream()
                        .allMatch(t -> t != null && t.matches(config.translationAllowedPattern())));
    }

    public Specification<Word> eachTranslationNotBlank() {
        return warnFalse("eachTranslationNotBlank",
                word -> word.getTranslations().stream()
                        .allMatch(t -> t != null && !t.isBlank()));
    }

    public Specification<Word> eachTranslationLengthInRange() {
        return warnFalse("eachTranslationLengthInRange",
                word -> word.getTranslations().stream()
                        .allMatch(t -> t != null
                                && t.length() >= config.translationMinLength()
                                && t.length() <= config.translationMaxLength()));
    }

    public Specification<Word> partOfSpeechNotNull() {
        return warnFalse("partOfSpeechNotNull",
                word -> word.getPartOfSpeech() != null);
    }

    public Specification<Word> targetRepetitionsNotNull() {
        return warnFalse("targetRepetitionsNotNull",
                word -> word.getTargetRepetitions() != null);
    }

    public Specification<Word> targetRepetitionsPositive() {
        return warnFalse("targetRepetitionsPositive",
                word -> word.getTargetRepetitions() != null
                        && word.getTargetRepetitions() > 0);
    }

    public Specification<Word> targetRepetitionsNotExceedingMax() {
        return warnFalse("targetRepetitionsNotExceedingMax",
                word -> word.getTargetRepetitions() != null
                        && word.getTargetRepetitions() <= config.maxTargetRepetitions());
    }

    public Specification<Word> currentRepetitionsNotNull() {
        return warnFalse("currentRepetitionsNotNull",
                word -> word.getCurrentRepetitions() != null);
    }

    public Specification<Word> currentRepetitionsNotNegative() {
        return warnFalse("currentRepetitionsNotNegative",
                word -> word.getCurrentRepetitions() != null
                        && word.getCurrentRepetitions() >= 0);
    }

    public Specification<Word> validForCreation() {
        return valueNotBlank()
                .and(valueMatchesPattern())
                .and(valueLengthInRange())
                .and(translationsNotNull())
                .and(translationsNotEmpty())
                .and(hasMinTranslations())
                .and(eachTranslationNotBlank())
                .and(translationsMatchPattern())
                .and(eachTranslationLengthInRange())
                .and(partOfSpeechNotNull())
                .and(targetRepetitionsNotNull())
                .and(targetRepetitionsPositive())
                .and(targetRepetitionsNotExceedingMax())
                .and(currentRepetitionsNotNull())
                .and(currentRepetitionsNotNegative());
    }
}
