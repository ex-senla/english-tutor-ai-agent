package com.hydroyura.eta.dictionary.application.config.properties;

import com.hydroyura.eta.dictionary.domain.word.WordSpecificationConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Accessors(fluent = true)
@ConfigurationProperties(prefix = "eta.dictionary.word")
public class WordSpecificationSpringProperties implements WordSpecificationConfig {

    private String valueAllowedPattern = "[a-zA-Z'\\- ]+";

    private int valueMinLength = 1;

    private int valueMaxLength = 50;

    private String translationAllowedPattern = "[а-яА-ЯёЁ ]+";

    private int translationMinLength = 1;

    private int translationMaxLength = 100;

    private int minTranslations = 1;

    private int maxTargetRepetitions = 100;

    private int defaultTargetRepetitions = 10;
}
