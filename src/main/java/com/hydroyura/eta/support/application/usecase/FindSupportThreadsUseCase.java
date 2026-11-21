package com.hydroyura.eta.support.application.usecase;

import com.hydroyura.eta.support.api.support.FindSupportThreads;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.api.support.SupportThreadInfo;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSupportThreadsUseCase implements FindSupportThreads {

    private final SupportThreadRepository supportThreadRepository;

    @Override
    public List<SupportThreadInfo> findAll() {
        return supportThreadRepository.findAll().stream()
                .map(OpenSupportThreadUseCase::toInfo)
                .toList();
    }

    @Override
    public Optional<SupportThreadInfo> findById(SupportThreadId threadId) {
        return supportThreadRepository.findById(threadId)
                .map(OpenSupportThreadUseCase::toInfo);
    }
}
