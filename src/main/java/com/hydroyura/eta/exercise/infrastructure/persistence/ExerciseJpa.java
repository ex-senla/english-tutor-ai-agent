package com.hydroyura.eta.exercise.infrastructure.persistence;

import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "exercise")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseJpa {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 32)
    ExerciseType exerciseType;

    @Column(name = "topic", nullable = false)
    String topic;

    @Column(name = "thematic_topic")
    String thematicTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    ExerciseStatus status;

    @Column(name = "content", columnDefinition = "text")
    String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_answers", nullable = false)
    String expectedAnswers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "items", nullable = false)
    String items;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "exercise_word_info", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "word_info_id", nullable = false)
    Set<UUID> wordInfoIds = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExerciseJpa other)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
