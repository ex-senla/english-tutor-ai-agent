package com.hydroyura.eta.student.api.student;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;

public record CreateStudentCommand(
        String name,
        DictionaryId dictionaryId
) {
}
