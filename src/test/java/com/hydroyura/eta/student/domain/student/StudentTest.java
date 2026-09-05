package com.hydroyura.eta.student.domain.student;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.StudentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentTest {

    @Test
    void shouldCreateStudent() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван");

        assertThat(student.getId()).isNotNull();
        assertThat(student.getName()).isEqualTo("Иван");
        assertThat(student.getDictionaryId()).isNotNull();
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Student.create(StudentId.generate(), DictionaryId.generate(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullDictionaryId() {
        assertThatThrownBy(() -> Student.create(StudentId.generate(), null, "Иван"))
                .isInstanceOf(NullPointerException.class);
    }
}
