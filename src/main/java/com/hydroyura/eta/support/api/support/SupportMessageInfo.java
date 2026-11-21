package com.hydroyura.eta.support.api.support;

import java.time.Instant;

public record SupportMessageInfo(SupportAuthor author, String text, Instant createdAt) {
}
