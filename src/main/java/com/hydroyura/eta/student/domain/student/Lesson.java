package com.hydroyura.eta.student.domain.student;

import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import java.time.Instant;
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
public class Lesson {

    @Identity
    private LessonId id;

    @Association
    private StudentId studentId;

    private String name;

    @Association
    private Set<WordId> wordIds = new HashSet<>();

    private LessonStatus status;

    private Instant startedAt;

    private Instant endedAt;

    private Lesson() {
    }

    public static Lesson start(LessonId id, StudentId studentId, String name) {
        Objects.requireNonNull(id, "LessonId must not be null");
        Objects.requireNonNull(studentId, "StudentId must not be null");
        Objects.requireNonNull(name, "Name must not be null");

        var lesson = new Lesson();
        lesson.id = id;
        lesson.studentId = studentId;
        lesson.name = name;
        lesson.status = LessonStatus.ACTIVE;
        lesson.startedAt = Instant.now();
        return lesson;
    }

    public void addWord(WordId wordId) {
        Objects.requireNonNull(wordId, "WordId must not be null");
        if (this.status != LessonStatus.ACTIVE) {
            throw new IllegalStateException("Lesson is not active");
        }
        this.wordIds.add(wordId);
    }

    public void end() {
        if (this.status != LessonStatus.ACTIVE) {
            throw new IllegalStateException("Lesson is not active");
        }
        this.status = LessonStatus.ENDED;
        this.endedAt = Instant.now();
    }

    public Set<WordId> getWordIds() {
        return Collections.unmodifiableSet(wordIds);
    }
}
