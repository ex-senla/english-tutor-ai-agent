package com.hydroyura.eta.exercise.domain.exercise;

import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

@Getter
@Entity
public class Exercise {

    @Identity
    private ExerciseId id;

    private ExerciseType type;

    private String topic;

    @Association
    private Set<WordId> wordIds = new HashSet<>();

    private String content;

    private String expectedAnswer;

    private ExerciseStatus status;

    private Exercise() {
    }

    public static Exercise create(ExerciseId id, ExerciseType type, String topic, Set<WordId> wordIds) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(wordIds, "wordIds must not be null");

        var exercise = new Exercise();
        exercise.id = id;
        exercise.type = type;
        exercise.topic = topic;
        exercise.wordIds = new HashSet<>(wordIds);
        exercise.status = ExerciseStatus.GENERATED;
        return exercise;
    }

    public void setContent(String content) {
        Objects.requireNonNull(content, "content must not be null");
        this.content = content;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        Objects.requireNonNull(expectedAnswer, "expectedAnswer must not be null");
        this.expectedAnswer = expectedAnswer;
    }

    public void markAnswered() {
        this.status = ExerciseStatus.ANSWERED;
    }

    public void markChecked() {
        this.status = ExerciseStatus.CHECKED;
    }

    public Set<WordId> getWordIds() {
        return Collections.unmodifiableSet(wordIds);
    }
}
