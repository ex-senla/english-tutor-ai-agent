package com.hydroyura.eta.teacher.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.CreateStudent;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import com.hydroyura.eta.teacher.api.teacher.IdentifierType;
import com.hydroyura.eta.teacher.domain.teacher.Teacher;
import com.hydroyura.eta.teacher.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateStudentWithDictionaryUseCaseTest {

    private CreateStudentWithDictionaryUseCase useCase;

    private StubTeacherRepository teacherRepository;

    private StubStudentRepository studentRepository;

    private StudentQuery studentQuery;

    @BeforeEach
    void setUp() {
        teacherRepository = new StubTeacherRepository();
        studentRepository = new StubStudentRepository();
        studentQuery = new StudentQuery() {

            public boolean existsByName(StudentExistsByNameQuery q) {
                return studentRepository.existsByNameInIds(q.studentIds(), q.name());
            }

            public Optional<StudentId> findByNameIn(FindStudentByNameQuery q) {
                return q.studentIds().stream().map(studentRepository::findById)
                        .flatMap(Optional::stream).filter(s -> s.getName().equalsIgnoreCase(q.name()))
                        .map(Student::getId).findFirst();
            }

            public Optional<com.hydroyura.eta.dictionary.api.dictionary.DictionaryId> getDictionaryId(StudentId sid) {
                return studentRepository.findById(sid).map(Student::getDictionaryId);
            }

            public java.util.List<StudentInfo> findStudentsByIds(Set<StudentId> ids) {
                return ids.stream()
                        .map(studentRepository::findById)
                        .flatMap(Optional::stream)
                        .map(s -> new StudentInfo(s.getId(), s.getName()))
                        .toList();
            }

            public Optional<StudentDetails> findStudentDetails(StudentId studentId) {
                return Optional.empty();
            }
        };

        var createDictionary = (CreateDictionary) cmd -> DictionaryId.generate();
        var createStudent = (CreateStudent) cmd -> {
            var id = StudentId.generate();
            studentRepository.save(Student.create(id, cmd.dictionaryId(), cmd.name()));
            return id;
        };

        useCase = new CreateStudentWithDictionaryUseCase(teacherRepository, studentQuery, createDictionary,
                createStudent);
    }

    @Test
    void shouldCreateStudentAndAddToTeacher() {
        var teacherId = TeacherId.generate();
        var teacher = com.hydroyura.eta.teacher.domain.teacher.Teacher.create(teacherId, "John");
        teacher.getIdentifiers().put(com.hydroyura.eta.teacher.api.teacher.IdentifierType.TELEGRAM, 123L);
        teacherRepository.save(teacher);

        var cmd = new CreateStudentWithDictionaryCommand(teacherId, "Иван", "Словарь Ивана");
        var studentId = useCase.execute(cmd);

        assertThat(studentId).isNotNull();
        var savedTeacher = teacherRepository.findById(teacherId).orElseThrow();
        assertThat(savedTeacher.getStudentIds()).contains(studentId);
    }

    @Test
    void shouldThrowWhenTeacherNotFound() {
        var cmd = new CreateStudentWithDictionaryCommand(TeacherId.generate(), "Иван", "Словарь");

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Teacher not found");
    }

    @Test
    void shouldRejectDuplicateStudentName() {
        var teacherId = TeacherId.generate();
        var teacher = Teacher.create(teacherId, "John");
        teacher.getIdentifiers().put(IdentifierType.TELEGRAM, 123L);
        var existingStudent = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван");
        studentRepository.save(existingStudent);
        teacher.addStudent(existingStudent.getId());
        teacherRepository.save(teacher);

        var cmd = new CreateStudentWithDictionaryCommand(teacherId, "Иван", "Словарь");

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // --- stub ---

    static class StubTeacherRepository implements TeacherRepository {

        private final Map<TeacherId, Teacher> store = new HashMap<>();

        @Override
        public Teacher save(Teacher t) {
            store.put(t.getId(), t);
            return t;
        }

        @Override
        public Optional<Teacher> findById(TeacherId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Teacher> findByIdentifier(IdentifierType type, Object value) {
            return store.values().stream().filter(t -> java.util.Objects.equals(value, t.getIdentifiers().get(type)))
                    .findFirst();
        }
    }

    static class StubStudentRepository implements StudentRepository {

        private final Map<StudentId, Student> store = new HashMap<>();

        @Override
        public Student save(Student s) {
            store.put(s.getId(), s);
            return s;
        }

        @Override
        public Optional<Student> findById(StudentId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean existsByNameInIds(Set<StudentId> ids, String name) {
            return ids.stream().map(store::get).anyMatch(s -> s != null && s.getName().equalsIgnoreCase(name));
        }
    }
}
