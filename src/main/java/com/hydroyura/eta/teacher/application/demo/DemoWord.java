package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import java.util.Set;

public record DemoWord(
        String value,
        Set<String> translations,
        PartOfSpeech partOfSpeech
) {
}
