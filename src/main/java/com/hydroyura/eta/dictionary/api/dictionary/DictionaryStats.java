package com.hydroyura.eta.dictionary.api.dictionary;

public record DictionaryStats(
    long totalWords,
    long newCount,
    long inProgressCount,
    long learnedCount
) {}
