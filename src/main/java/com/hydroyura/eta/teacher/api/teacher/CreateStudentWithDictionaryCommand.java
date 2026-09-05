package com.hydroyura.eta.teacher.api.teacher;

public record CreateStudentWithDictionaryCommand(
        TeacherId teacherId,
        String studentName,
        String dictionaryName
) {
}
