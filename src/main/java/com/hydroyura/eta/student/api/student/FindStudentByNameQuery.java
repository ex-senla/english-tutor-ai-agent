package com.hydroyura.eta.student.api.student;

import java.util.Set;

public record FindStudentByNameQuery(
        Set<StudentId> studentIds,
        String name
) {
}
