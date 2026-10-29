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
                        && valueAllowedPattern.matcher(word.getValue()).matches());
    }

    public Specification<Word> valueLengthInRange() {
        return warnFalse("valueLengthInRange",
                word -> word.getValue() != null
                        && word.getValue().length() >= valueMinLength
                        && word.getValue().length() <= valueMaxLength);
    }

    public Specification<Word> validForCreation() {
        return valueNotBlank()
                .and(valueMatchesPattern())
                .and(valueLengthInRange());
    }
}
