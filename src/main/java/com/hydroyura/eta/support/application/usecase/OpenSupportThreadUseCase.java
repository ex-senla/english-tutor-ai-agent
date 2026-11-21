package com.hydroyura.eta.support.application.usecase;

import com.hydroyura.eta.support.api.support.OpenSupportThread;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.api.support.SupportThreadInfo;
import com.hydroyura.eta.support.domain.thread.SupportThread;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class OpenSupportThreadUseCase implements OpenSupportThread {

    private final SupportThreadRepository supportThreadRepository;

    @Override
    public SupportThreadInfo open(TeacherId teacherId) {
        var thread = supportThreadRepository.findByTeacherId(teacherId)
                .orElseGet(() -> {
                    var created = SupportThread.open(SupportThreadId.generate(), teacherId, Instant.now());
                    log.info("Support thread opened for teacher {}", teacherId.value());
                    return supportThreadRepository.save(created);
                });
        return toInfo(thread);
    }

    static SupportThreadInfo toInfo(SupportThread thread) {
        return new SupportThreadInfo(thread.getId(), thread.getTeacherId(), thread.getCreatedAt(),
                thread.getLastMessageAt());
    }
}
