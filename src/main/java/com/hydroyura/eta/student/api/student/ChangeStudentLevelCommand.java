package com.hydroyura.eta.student.api.student;

public record ChangeStudentLevelCommand(
        StudentId studentId,
        CefrLevel level
) {
}
