package com.hydroyura.eta.support.api.support;

public record AppendSupportMessageCommand(SupportThreadId threadId, SupportAuthor author, String text) {
}
