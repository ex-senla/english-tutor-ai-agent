package com.hydroyura.eta.student.api.student;

import com.hydroyura.eta.shared.api.DomainException;

/**
 * Узкое доменное исключение: у учителя уже есть не-удалённый ученик с таким именем
 * (без учёта регистра). Находится в {@code student.api.student}, чтобы быть доступным
 * модулям teacher (бросает) и chatbot (ловит) без нарушения границ Modulith.
 */
public class StudentAlreadyExistsException extends DomainException {

    public StudentAlreadyExistsException(String studentName) {
        super("Student '" + studentName + "' already exists");
    }
}
