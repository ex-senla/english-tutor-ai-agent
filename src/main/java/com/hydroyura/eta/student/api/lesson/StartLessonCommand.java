package com.hydroyura.eta.student.api.lesson;

import com.hydroyura.eta.student.api.student.StudentId;

public record StartLessonCommand(
        StudentId studentId,
        String name
) {
}
