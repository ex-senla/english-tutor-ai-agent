package com.hydroyura.eta.support.application.usecase;

import com.hydroyura.eta.support.api.support.AppendSupportMessage;
import com.hydroyura.eta.support.api.support.AppendSupportMessageCommand;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppendSupportMessageUseCase implements AppendSupportMessage {

    private final SupportThreadRepository supportThreadRepository;

    @Override
    public void append(AppendSupportMessageCommand command) {
        var threadId = Objects.requireNonNull(command.threadId(), "threadId must not be null");
        var author = Objects.requireNonNull(command.author(), "author must not be null");
        var text = Objects.requireNonNull(command.text(), "text must not be null");

        var thread = supportThreadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Support thread not found: " + threadId));

        thread.appendMessage(author, text, Instant.now());
        supportThreadRepository.save(thread);
        log.info("Support message appended to thread {} by {}", threadId.value(), author);
    }
}
