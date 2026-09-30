package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FindActiveLessonServiceTest {

    private FindActiveLesson findActiveLesson;

    private StubLessonRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubLessonRepository();
        findActiveLesson = new FindActiveLessonService(repository);
    }

    @Test
    void shouldReturnLessonIdWhenLessonIsActive() {
        var studentId = StudentId.generate();
        var lesson = Lesson.start(LessonId.generate(), studentId, "Test Lesson");
        repository.save(lesson);

        var result = findActiveLesson.findByStudentId(studentId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(lesson.getId());
    }

    @Test
    void shouldReturnEmptyWhenNoActiveLesson() {
        var studentId = StudentId.generate();

        var result = findActiveLesson.findByStudentId(studentId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenLessonIsEnded() {
        var studentId = StudentId.generate();
        var lesson = Lesson.start(LessonId.generate(), studentId, "Test Lesson");
        lesson.end();
        repository.save(lesson);

        var result = findActiveLesson.findByStudentId(studentId);

        assertThat(result).isEmpty();
    }

    // --- stub ---

    static class StubLessonRepository implements LessonRepository {

        private final Map<LessonId, Lesson> store = new HashMap<>();

        @Override
        public Lesson save(Lesson l) {
            store.put(l.getId(), l);
            return l;
        }

        @Override
        public Optional<Lesson> findById(LessonId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Lesson> findActiveByStudentId(StudentId sid) {
            return store.values().stream()
                    .filter(l -> l.getStudentId().equals(sid)
                            && l.getStatus() == com.hydroyura.eta.student.domain.student.LessonStatus.ACTIVE)
                    .findFirst();
        }
    }
}
