package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.shared.api.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public final class WordSpecifications {

    private final Pattern valueAllowedPattern;

    private final int valueMinLength;

    private final int valueMaxLength;

    private Specification<Word> warnFalse(String name, Specification<Word> spec) {
        return word -> {
            boolean satisfied = spec.isSatisfiedBy(word);
            if (!satisfied) {
                log.warn("Word validation failed: {} for word '{}'", name, word.value());
            }
            return satisfied;
        };
    }

    public Specification<Word> valueNotBlank() {
        return warnFalse("valueNotBlank",
                word -> word.value() != null && !word.value().isBlank());
    }

    public Specification<Word> valueMatchesPattern() {
        return warnFalse("valueMatchesPattern",
                word -> word.value() != null
                        && valueAllowedPattern.matcher(word.value()).matches());
    }

    public Specification<Word> valueLengthInRange() {
        return warnFalse("valueLengthInRange",
                word -> word.value() != null
                        && word.value().length() >= valueMinLength
                        && word.value().length() <= valueMaxLength);
    }

    public Specification<Word> validForCreation() {
        return valueNotBlank()
                .and(valueMatchesPattern())
                .and(valueLengthInRange());
    }
}
