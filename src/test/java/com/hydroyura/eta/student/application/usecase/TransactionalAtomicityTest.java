package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.DeleteStudent;
import com.hydroyura.eta.student.api.student.DeleteStudentCommand;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import com.hydroyura.eta.student.domain.student.exception.StudentDeletionNotAllowedException;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionalAtomicityTest extends PostgresIntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CreateStudentWithDictionary createStudentWithDictionary;

    @Autowired
    private DeleteStudent deleteStudent;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM exercise_word_info");
        jdbcTemplate.execute("DELETE FROM word_info_translation");
        jdbcTemplate.execute("DELETE FROM word_info");
        jdbcTemplate.execute("DELETE FROM lesson_word_info");
        jdbcTemplate.execute("DELETE FROM lesson");
        jdbcTemplate.execute("DELETE FROM student");
        jdbcTemplate.execute("DELETE FROM teacher_identifier");
        jdbcTemplate.execute("DELETE FROM teacher_student");
        jdbcTemplate.execute("DELETE FROM teacher");
        jdbcTemplate.execute("DELETE FROM dictionary");
        jdbcTemplate.execute("DELETE FROM exercise");
        jdbcTemplate.execute("DELETE FROM chat");
    }

    @Test
    void shouldRollbackCreateStudentWithDictionaryOnFailure() {
        var teacher = Teacher.create(TeacherId.generate(), "John");
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, 123L);
        teacherRepository.save(teacher);

        assertThatThrownBy(() -> createStudentWithDictionary.execute(
                new CreateStudentWithDictionaryCommand(teacher.getId(), "", "Словарь", CefrLevel.A2)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dictionary", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM student", Integer.class)).isZero();
        assertThat(teacherRepository.findById(teacher.getId()).orElseThrow().getStudentIds()).isEmpty();
    }

    @Test
    void shouldNotDeleteStudentWithActiveLesson() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.A2);
        studentRepository.save(student);
        lessonRepository.save(Lesson.start(LessonId.generate(), student.getId(), "Lesson"));

        assertThatThrownBy(() -> deleteStudent.execute(new DeleteStudentCommand(student.getId())))
                .isInstanceOf(StudentDeletionNotAllowedException.class);

        assertThat(studentRepository.findById(student.getId()).orElseThrow().isDeleted()).isFalse();
    }
}
