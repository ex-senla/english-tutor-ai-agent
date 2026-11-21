package com.hydroyura.eta.support.domain.thread;

import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.List;
import java.util.Optional;

public interface SupportThreadRepository {

    SupportThread save(SupportThread thread);

    Optional<SupportThread> findById(SupportThreadId id);

    Optional<SupportThread> findByTeacherId(TeacherId teacherId);

    List<SupportThread> findAll();
}
