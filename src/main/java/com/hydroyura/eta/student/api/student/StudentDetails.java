package com.hydroyura.eta.student.api.student;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;

public record StudentDetails(
    String name,
    DictionaryStats dictionaryStats,
    boolean hasActiveLesson
) {}
