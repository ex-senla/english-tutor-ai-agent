package com.hydroyura.eta.student.api.lesson;

import com.hydroyura.eta.dictionary.api.word.WordId;
import java.time.Instant;
import java.util.Set;

public record EndLessonResult(
    Instant startedAt,
    Instant endedAt,
    Set<WordId> wordIds
) {}
