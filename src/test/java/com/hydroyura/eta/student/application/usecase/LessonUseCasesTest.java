package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.student.api.lesson.*;
import com.hydroyura.eta.student.api.student.*;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.api.student.StudentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LessonUseCasesTest {

    private StartLessonUseCase startLesson;

    private AddWordToLessonUseCase addWordToLesson;

    private EndLessonUseCase endLesson;

    private StubLessonRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubLessonRepository();
        startLesson = new StartLessonUseCase(repository);
        addWordToLesson = new AddWordToLessonUseCase(repository);
        endLesson = new EndLessonUseCase(repository);
    }

    @Test
    void shouldFullLessonLifecycle() {
        // start
        var lessonId = startLesson.execute(
                new StartLessonCommand(StudentId.generate(), "Lesson 1"));

        assertThat(lessonId).isNotNull();

        // add word
        addWordToLesson.execute(new AddWordToLessonCommand(lessonId, WordId.generate()));

        var lesson = repository.findById(lessonId).orElseThrow();
        assertThat(lesson.getWordIds()).hasSize(1);

        // end
        endLesson.execute(new EndLessonCommand(lessonId));

        lesson = repository.findById(lessonId).orElseThrow();
        assertThat(lesson.getStatus()).isEqualTo(
                com.hydroyura.eta.student.domain.student.LessonStatus.ENDED);
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
            return Optional.empty();
        }
    }
}
