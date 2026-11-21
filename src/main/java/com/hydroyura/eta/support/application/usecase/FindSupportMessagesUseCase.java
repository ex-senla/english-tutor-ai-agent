package com.hydroyura.eta.support.application.usecase;

import com.hydroyura.eta.support.api.support.FindSupportMessages;
import com.hydroyura.eta.support.api.support.SupportMessageInfo;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSupportMessagesUseCase implements FindSupportMessages {

    private final SupportThreadRepository supportThreadRepository;

    @Override
    public List<SupportMessageInfo> findByThreadId(SupportThreadId threadId) {
        Objects.requireNonNull(threadId, "threadId must not be null");
        return supportThreadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Support thread not found: " + threadId))
                .getMessages().stream()
                .map(m -> new SupportMessageInfo(m.author(), m.text(), m.createdAt()))
                .toList();
    }
}
