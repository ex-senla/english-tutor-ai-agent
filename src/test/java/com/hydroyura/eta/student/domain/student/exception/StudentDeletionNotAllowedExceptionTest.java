package com.hydroyura.eta.student.domain.student.exception;

import com.hydroyura.eta.shared.api.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentDeletionNotAllowedExceptionTest {

    @Test
    void shouldBeDomainExceptionWithMessage() {
        var exception = new StudentDeletionNotAllowedException("Завершите урок");

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getMessage()).isEqualTo("Завершите урок");
    }
}
