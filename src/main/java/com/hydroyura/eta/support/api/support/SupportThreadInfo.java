package com.hydroyura.eta.support.api.support;

import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.time.Instant;

public record SupportThreadInfo(
        SupportThreadId id,
        TeacherId teacherId,
        Instant createdAt,
        Instant lastMessageAt) {
}
