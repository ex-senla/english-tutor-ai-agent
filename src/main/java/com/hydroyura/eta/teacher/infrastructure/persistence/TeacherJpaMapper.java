package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class TeacherJpaMapper {

    TeacherJpa toEntity(Teacher domain) {
        var entity = new TeacherJpa();
        entity.id = domain.getId().value();
        sync(entity, domain);
        return entity;
    }

    void sync(TeacherJpa entity, Teacher domain) {
        entity.name = domain.getName();
        entity.getStudentIds().clear();
        domain.getStudentIds().forEach(id -> entity.getStudentIds().add(id.value()));
        syncIdentifiers(entity, domain.getIdentifiers().asMap());
    }

    private void syncIdentifiers(TeacherJpa entity, Map<IdentifierType, Object> identifiers) {
        var byType = entity.getIdentifiers().stream()
                .collect(Collectors.toMap(i -> i.getId().getIdentifierType(), Function.identity()));

        entity.getIdentifiers().removeIf(i -> !identifiers.containsKey(i.getId().getIdentifierType()));

        for (var entry : identifiers.entrySet()) {
            var existing = byType.get(entry.getKey());
            if (existing == null) {
                var identifier = new TeacherIdentifierJpa();
                identifier.id = new TeacherIdentifierId(entity.id, entry.getKey());
                identifier.identifierValue = valueToString(entry.getKey(), entry.getValue());
                identifier.teacher = entity;
                entity.getIdentifiers().add(identifier);
            } else {
                existing.identifierValue = valueToString(entry.getKey(), entry.getValue());
            }
        }
    }

    Teacher toDomain(TeacherJpa entity) {
        var teacher = Teacher.create(new TeacherId(entity.getId()), entity.getName());
        entity.getStudentIds().forEach(id -> teacher.addStudent(new StudentId(id)));
        for (var identifier : entity.getIdentifiers()) {
            var type = identifier.getId().getIdentifierType();
            teacher.getIdentifiers().put(type, valueFromString(type, identifier.getIdentifierValue()));
        }
        return teacher;
    }

    static String valueToString(IdentifierType type, Object value) {
        if (!type.getValueClass().isInstance(value)) {
            throw new IllegalStateException(
                    "Identifier value for " + type + " must be " + type.getValueClass().getSimpleName());
        }
        return String.valueOf(value);
    }

    static Object valueFromString(IdentifierType type, String raw) {
        var valueClass = type.getValueClass();
        if (valueClass == Long.class) {
            try {
                return Long.valueOf(raw);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Cannot convert identifier value '" + raw + "' for " + type, e);
            }
        }
        if (valueClass == String.class) {
            return raw;
        }
        throw new IllegalStateException("Unsupported identifier value class " + valueClass.getSimpleName());
    }
}
