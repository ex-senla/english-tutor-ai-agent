package com.hydroyura.eta.student.infrastructure.persistence;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.student.api.lesson.LessonId;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.domain.student.Lesson;
import com.hydroyura.eta.student.domain.student.LessonRepository;
import com.hydroyura.eta.student.domain.student.LessonStatus;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class StudentLessonPersistenceRoundTripTest extends PostgresIntegrationTestBase {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    void shouldRoundTripStudentIncludingDeletedFlag() {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Иван", CefrLevel.B2);
        studentRepository.save(student);

        var loaded = studentRepository.findById(student.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Иван");
        assertThat(loaded.getLevel()).isEqualTo(CefrLevel.B2);
        assertThat(loaded.getDictionaryId()).isEqualTo(student.getDictionaryId());
        assertThat(loaded.isDeleted()).isFalse();

        loaded.markDeleted();
        studentRepository.save(loaded);

        var deleted = studentRepository.findById(student.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    void shouldRoundTripLessonWithWordsAndStatus() {
        var studentId = StudentId.generate();
        studentRepository.save(Student.create(studentId, DictionaryId.generate(), "Иван", CefrLevel.A2));
        var wordA = WordInfoId.generate();
        var wordB = WordInfoId.generate();

        var lesson = Lesson.start(LessonId.generate(), studentId, "Lesson 1");
        lesson.addWord(wordA);
        lesson.addWord(wordB);
        lessonRepository.save(lesson);

        var active = lessonRepository.findActiveByStudentId(studentId).orElseThrow();
        assertThat(active.getId()).isEqualTo(lesson.getId());
        assertThat(active.getWordInfoIds()).containsExactlyInAnyOrder(wordA, wordB);

        active.end();
        lessonRepository.save(active);

        var ended = lessonRepository.findById(lesson.getId()).orElseThrow();
        assertThat(ended.getStatus()).isEqualTo(LessonStatus.ENDED);
        assertThat(ended.getEndedAt()).isNotNull();
        assertThat(ended.getStartedAt()).isEqualTo(active.getStartedAt());
        assertThat(ended.getWordInfoIds()).containsExactlyInAnyOrder(wordA, wordB);

        assertThat(lessonRepository.findActiveByStudentId(studentId)).isEmpty();
    }
}
