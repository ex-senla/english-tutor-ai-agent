package com.hydroyura.eta.teacher.infrastructure.persistence;

import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherIdentifierId {

    @Column(name = "teacher_id", nullable = false)
    UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type", nullable = false, length = 32)
    IdentifierType identifierType;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeacherIdentifierId other)) {
            return false;
        }
        return Objects.equals(teacherId, other.teacherId) && identifierType == other.identifierType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, identifierType);
    }
}
