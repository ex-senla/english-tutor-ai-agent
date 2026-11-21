package com.hydroyura.eta.support.api.support;

import java.util.List;

public interface FindSupportMessages {

    List<SupportMessageInfo> findByThreadId(SupportThreadId threadId);
}
