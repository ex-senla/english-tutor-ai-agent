package com.hydroyura.eta.support.api.support;

import com.hydroyura.eta.teacher.api.teacher.TeacherId;

public interface OpenSupportThread {

    SupportThreadInfo open(TeacherId teacherId);
}
