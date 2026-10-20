package com.hydroyura.eta.teacher.api.teacher;

import com.hydroyura.eta.student.api.student.CefrLevel;

public record CreateStudentWithDictionaryCommand(
        TeacherId teacherId,
        String studentName,
        String dictionaryName,
        CefrLevel level
) {
}
