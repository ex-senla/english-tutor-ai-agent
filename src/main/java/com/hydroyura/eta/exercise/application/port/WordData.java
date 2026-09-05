package com.hydroyura.eta.exercise.application.port;

import java.util.Set;

public record WordData(String value, Set<String> translations, String partOfSpeech) {
}
