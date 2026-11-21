package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Student;

final class StudentJpaMapper {

    StudentJpa toEntity(Student domain) {
        var entity = new StudentJpa();
        entity.id = domain.getId().value();
        sync(entity, domain);
        return entity;
    }

    void sync(StudentJpa entity, Student domain) {
        entity.dictionaryId = domain.getDictionaryId().value();
        entity.name = domain.getName();
        entity.cefrLevel = domain.getLevel();
        entity.deleted = domain.isDeleted();
    }

    Student toDomain(StudentJpa entity) {
        var student = Student.create(
                new StudentId(entity.getId()),
                new DictionaryId(entity.getDictionaryId()),
                entity.getName(),
                entity.getCefrLevel());
        if (entity.isDeleted()) {
            student.markDeleted();
        }
        return student;
    }
}
