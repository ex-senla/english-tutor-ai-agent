package com.hydroyura.eta.teacher.infrastructure.persistence;

import java.util.UUID;

/**
 * Конструкторная JPQL-проекция для лёгкого списка учителей.
 */
public record TeacherRow(UUID id, String name, boolean admin) {
}
