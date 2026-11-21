package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.TeacherInfo;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaTeacherRepository implements TeacherRepository {

    private final TeacherDataRepository dataRepository;

    private final TeacherJpaMapper mapper = new TeacherJpaMapper();

    private final EntityManager entityManager;

    public JpaTeacherRepository(TeacherDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Teacher save(Teacher teacher) {
        var id = teacher.getId().value();
        var existing = dataRepository.findWithChildren(id);
        if (existing.isEmpty()) {
            entityManager.persist(mapper.toEntity(teacher));
        } else {
            mapper.sync(existing.get(), teacher);
        }
        return teacher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> findById(TeacherId id) {
        return dataRepository.findWithChildren(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> findByIdentifier(IdentifierType type, Object value) {
        var raw = TeacherJpaMapper.valueToString(type, value);
        return dataRepository.findByIdentifier(type, raw).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherInfo> findAllInfos() {
        return dataRepository.findAllInfos().stream()
                .map(row -> new TeacherInfo(new TeacherId(row.id()), row.name(), row.admin()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findTelegramChatId(TeacherId id) {
        return dataRepository.findIdentifierValue(id.value(), IdentifierType.TELEGRAM)
                .map(TeacherJpaMapper::parseLong);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<StudentId> findStudentIds(TeacherId id) {
        return dataRepository.findStudentIds(id.value()).stream()
                .map(StudentId::new)
                .collect(java.util.stream.Collectors.toSet());
    }
}
