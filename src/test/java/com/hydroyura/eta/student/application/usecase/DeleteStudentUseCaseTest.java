package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.DeleteStudentCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import com.hydroyura.eta.student.domain.student.exception.StudentDeletionNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteStudentUseCaseTest {

    private DeleteStudentUseCase useCase;

    private StubStudentRepository studentRepository;

    private StubLessonRepository lessonRepository;

    @BeforeEach
    void setUp() {
        studentRepository = new StubStudentRepository();
        lessonRepository = new StubLessonRepository();
        useCase = new DeleteStudentUseCase(studentRepository, lessonRepository);
    }

    @Test
    void shouldDeleteExistingStudent() {
        var student = saveStudent("Иван");

        useCase.execute(new DeleteStudentCommand(student.getId()));

        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void shouldDeleteSecondTimeWithoutError() {
        var student = saveStudent("Иван");

        useCase.execute(new DeleteStudentCommand(student.getId()));
        useCase.execute(new DeleteStudentCommand(student.getId()));

        var saved = studentRepository.findById(student.getId()).orElseThrow();
        assertThat(saved.isDeleted()).isTrue();
        assertThat(saved.getName()).isEqualTo("Иван");
        assertThat(saved.getLevel()).isEqualTo(CefrLevel.A2);
    }

    @Test
    void shouldDeleteThirdTimeIdempotently() {
        var student = saveStudent("Иван");

        useCase.execute(new DeleteStudentCommand(student.getId()));
        useCase.execute(new DeleteStudentCommand(student.getId()));
        useCase.execute(new DeleteStudentCommand(student.getId()));

        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void shouldThrowWhenStudentNotFound() {
        assertThatThrownBy(() -> useCase.execute(new DeleteStudentCommand(StudentId.generate())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    void shouldBlockWhenActiveLessonExists() {
        var student = saveStudent("Иван");
        lessonRepository.putActive(student.getId());

        assertThatThrownBy(() -> useCase.execute(new DeleteStudentCommand(student.getId())))
                .isInstanceOf(StudentDeletionNotAllowedException.class);
        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isFalse();
    }

    @Test
    void shouldAllowWhenLessonEnded() {
        var student = saveStudent("Иван");
        var lesson = Lesson.start(LessonId.generate(), student.getId(), "Lesson");
        lesson.end();
        lessonRepository.save(lesson);

        useCase.execute(new DeleteStudentCommand(student.getId()));

        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void shouldAllowWhenNoLessons() {
        var student = saveStudent("Иван");

        useCase.execute(new DeleteStudentCommand(student.getId()));

        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void shouldBeIdempotentEvenWithActiveLessonWhenAlreadyDeleted() {
        var student = saveStudent("Иван");
        student.markDeleted();
        studentRepository.save(student);
        lessonRepository.putActive(student.getId());

        assertThatCode(() -> useCase.execute(new DeleteStudentCommand(student.getId())))
                .doesNotThrowAnyException();
        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullStudentId() {
        assertThatThrownBy(() -> useCase.execute(new DeleteStudentCommand(null)))
                .isInstanceOf(NullPointerException.class);
    }

    private Student saveStudent(String name) {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), name, CefrLevel.A2);
        studentRepository.save(student);
        return student;
    }

    static class StubStudentRepository implements StudentRepository {

        private final Map<StudentId, Student> store = new HashMap<>();

        @Override
        public Student save(Student student) {
            store.put(student.getId(), student);
            return student;
        }

        @Override
        public Optional<Student> findById(StudentId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean existsByNameInIds(Set<StudentId> ids, String name) {
            return ids.stream().map(store::get)
                    .anyMatch(s -> s != null && !s.isDeleted() && s.getName().equalsIgnoreCase(name));
        }
    }

    static class StubLessonRepository implements LessonRepository {

        private final Map<StudentId, Lesson> activeByStudent = new HashMap<>();

        @Override
        public Lesson save(Lesson lesson) {
            return lesson;
        }

        @Override
        public Optional<Lesson> findById(LessonId id) {
            return Optional.empty();
        }

        @Override
        public Optional<Lesson> findActiveByStudentId(StudentId studentId) {
            return Optional.ofNullable(activeByStudent.get(studentId));
        }

        void putActive(StudentId studentId) {
            activeByStudent.put(studentId, Lesson.start(LessonId.generate(), studentId, "Lesson"));
        }
    }
}
