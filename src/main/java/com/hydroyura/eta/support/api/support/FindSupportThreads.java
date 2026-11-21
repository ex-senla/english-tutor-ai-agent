package com.hydroyura.eta.support.api.support;

import java.util.List;
import java.util.Optional;

public interface FindSupportThreads {

    List<SupportThreadInfo> findAll();

    Optional<SupportThreadInfo> findById(SupportThreadId threadId);
}
