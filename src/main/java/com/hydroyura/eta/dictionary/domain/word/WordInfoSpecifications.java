package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.shared.api.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public final class WordInfoSpecifications {

    private final Pattern translationAllowedPattern;

    private final int translationMinLength;

    private final int translationMaxLength;

    private final int minTranslations;

    private final int maxTargetRepetitions;

    private Specification<WordInfo> warnFalse(String name, Specification<WordInfo> spec) {
        return word -> {
            boolean satisfied = spec.isSatisfiedBy(word);
            if (!satisfied) {
                log.warn("Word validation failed: {} for word '{}'", name, word.getWord().getValue());
            }
            return satisfied;
        };
    }

    public Specification<WordInfo> translationsNotNull() {
        return warnFalse("translationsNotNull",
                word -> word.getTranslations() != null);
    }

    public Specification<WordInfo> translationsNotEmpty() {
        return warnFalse("translationsNotEmpty",
                word -> !word.getTranslations().isEmpty());
    }

    public Specification<WordInfo> hasMinTranslations() {
        return warnFalse("hasMinTranslations",
                word -> word.getTranslations().size() >= minTranslations);
    }

    public Specification<WordInfo> translationsMatchPattern() {
        return warnFalse("translationsMatchPattern",
                word -> word.getTranslations().stream()
                        .allMatch(t -> t != null && translationAllowedPattern.matcher(t).matches()));
    }

    public Specification<WordInfo> eachTranslationNotBlank() {
        return warnFalse("eachTranslationNotBlank",
                word -> word.getTranslations().stream()
                        .allMatch(t -> t != null && !t.isBlank()));
    }

    public Specification<WordInfo> eachTranslationLengthInRange() {
        return warnFalse("eachTranslationLengthInRange",
                word -> word.getTranslations().stream()
                        .allMatch(t -> t != null
                                && t.length() >= translationMinLength
                                && t.length() <= translationMaxLength));
    }

    public Specification<WordInfo> partOfSpeechNotNull() {
        return warnFalse("partOfSpeechNotNull",
                word -> word.getPartOfSpeech() != null);
    }

    public Specification<WordInfo> targetRepetitionsNotNull() {
        return warnFalse("targetRepetitionsNotNull",
                word -> word.getTargetRepetitions() != null);
    }

    public Specification<WordInfo> targetRepetitionsPositive() {
        return warnFalse("targetRepetitionsPositive",
                word -> word.getTargetRepetitions() != null
                        && word.getTargetRepetitions() > 0);
    }

    public Specification<WordInfo> targetRepetitionsNotExceedingMax() {
        return warnFalse("targetRepetitionsNotExceedingMax",
                word -> word.getTargetRepetitions() != null
                        && word.getTargetRepetitions() <= maxTargetRepetitions);
    }

    public Specification<WordInfo> currentRepetitionsNotNull() {
        return warnFalse("currentRepetitionsNotNull",
                word -> word.getCurrentRepetitions() != null);
    }

    public Specification<WordInfo> currentRepetitionsNotNegative() {
        return warnFalse("currentRepetitionsNotNegative",
                word -> word.getCurrentRepetitions() != null
                        && word.getCurrentRepetitions() >= 0);
    }

    public Specification<WordInfo> validForCreation() {
        return translationsNotNull()
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
