package com.hydroyura.eta.exercise.domain.exercise;

import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    private List<String> expectedAnswers = new ArrayList<>();

    private List<ExerciseItem> items = new ArrayList<>();

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

    public void setExpectedAnswers(List<String> expectedAnswers) {
        Objects.requireNonNull(expectedAnswers, "expectedAnswers must not be null");
        this.expectedAnswers = new ArrayList<>(expectedAnswers);
    }

    public void setItems(List<ExerciseItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        this.items = new ArrayList<>(items);
    }

    public void markAnswered() {
        this.status = ExerciseStatus.ANSWERED;
    }

    public void markChecked() {
        this.status = ExerciseStatus.CHECKED;
    }

    public List<String> getExpectedAnswers() {
        return Collections.unmodifiableList(expectedAnswers);
    }

    public List<ExerciseItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Set<WordId> getWordIds() {
        return Collections.unmodifiableSet(wordIds);
    }
}
