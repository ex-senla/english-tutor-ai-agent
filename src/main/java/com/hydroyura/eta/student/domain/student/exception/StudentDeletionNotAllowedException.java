package com.hydroyura.eta.student.domain.student.exception;

import com.hydroyura.eta.shared.api.DomainException;

/**
 * Удаление ученика заблокировано: у него есть активный урок.
 */
public class StudentDeletionNotAllowedException extends DomainException {

    public StudentDeletionNotAllowedException(String message) {
        super(message);
    }
}
