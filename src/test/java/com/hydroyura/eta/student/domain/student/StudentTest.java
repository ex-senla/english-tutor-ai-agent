package com.hydroyura.eta.student.domain.student;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentTest {

    @Test
    void shouldCreateStudent() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.A2);

        assertThat(student.getId()).isNotNull();
        assertThat(student.getName()).isEqualTo("Иван");
        assertThat(student.getDictionaryId()).isNotNull();
        assertThat(student.getLevel()).isEqualTo(CefrLevel.A2);
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Student.create(StudentId.generate(), DictionaryId.generate(), null, CefrLevel.A2))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullDictionaryId() {
        assertThatThrownBy(() -> Student.create(StudentId.generate(), null, "Иван", CefrLevel.A2))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullLevel() {
        assertThatThrownBy(() -> Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldChangeLevel() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.A2);

        student.changeLevel(CefrLevel.B1);

        assertThat(student.getLevel()).isEqualTo(CefrLevel.B1);
    }

    @Test
    void shouldRejectNullLevelOnChange() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.A2);

        assertThatThrownBy(() -> student.changeLevel(null))
                .isInstanceOf(NullPointerException.class);
    }
}
