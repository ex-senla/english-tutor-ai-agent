package com.hydroyura.eta.student.api.student;

import java.util.Set;

public record StudentExistsByNameQuery(
        Set<StudentId> studentIds,
        String name
) {
}
