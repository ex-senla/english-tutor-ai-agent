package com.hydroyura.eta.student.api.lesson;

import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import java.time.Instant;
import java.util.Set;

public record EndLessonResult(
        Instant startedAt,
        Instant endedAt,
        Set<WordInfoId> wordInfoIds
) {
}
