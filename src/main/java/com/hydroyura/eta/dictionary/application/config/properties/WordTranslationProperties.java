package com.hydroyura.eta.dictionary.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eta.dictionary.translation")
public class WordTranslationProperties {

    private int variantCount = 3;
}
