package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.Word;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.SearchStudentsQuery;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class StudentQueryServiceQueryCountTest extends PostgresIntegrationTestBase {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private StudentQuery studentQuery;

    @Test
    void shouldUseConstantQueriesForFindStudentsByIds() {
        var small = seedActiveStudents(5);
        var large = seedActiveStudents(15);

        clearSession();
        var smallWindow = measureWindow(() -> studentQuery.findStudentsByIds(small));
        clearSession();
        var largeWindow = measureWindow(() -> studentQuery.findStudentsByIds(large));

        assertSameQueryCount(smallWindow, largeWindow, 2);
    }

    @Test
    void shouldUseConstantQueriesForSearchByNameFragment() {
        var small = seedActiveStudents(5);
        var large = seedActiveStudents(15);

        clearSession();
        var smallWindow = measureWindow(() -> studentQuery.searchByNameFragment(new SearchStudentsQuery(small, "s")));
        clearSession();
        var largeWindow = measureWindow(() -> studentQuery.searchByNameFragment(new SearchStudentsQuery(large, "s")));

        assertSameQueryCount(smallWindow, largeWindow, 2);
    }

    @Test
    void shouldUseConstantQueriesForFindByNameIn() {
        var small = seedActiveStudents(5);
        var large = seedActiveStudents(15);

        clearSession();
        var smallWindow = measureWindow(() -> studentQuery.findByNameIn(new FindStudentByNameQuery(small, "Student1")));
        clearSession();
        var largeWindow = measureWindow(() -> studentQuery.findByNameIn(new FindStudentByNameQuery(large, "Student1")));

        assertSameQueryCount(smallWindow, largeWindow, 2);
    }

    @Test
    void shouldUseConstantQueriesForFindStudentDetails() {
        var smallStudent = seedStudentWithDictionary(10);
        var largeStudent = seedStudentWithDictionary(40);

        clearSession();
        var smallWindow = measureWindow(() -> studentQuery.findStudentDetails(smallStudent));
        clearSession();
        var largeWindow = measureWindow(() -> studentQuery.findStudentDetails(largeStudent));

        assertSameQueryCount(smallWindow, largeWindow, 3);
    }

    @Test
    void shouldUseSingleQueryForExistsByName() {
        var ids = seedActiveStudents(15);

        clearSession();
        var window = measureWindow(() -> studentQuery.existsByName(new StudentExistsByNameQuery(ids, "Student1")));

        assertThat(window.prepare()).isLessThanOrEqualTo(2);
        assertThat(window.lazyFetches()).isZero();
    }

    private void assertSameQueryCount(QueryWindow small, QueryWindow large, long ceiling) {
        assertThat(large.prepare()).isEqualTo(small.prepare());
        assertThat(large.queryExecution()).isEqualTo(small.queryExecution());
        assertThat(small.prepare()).isLessThanOrEqualTo(ceiling);
        assertThat(small.lazyFetches()).isZero();
        assertThat(large.lazyFetches()).isZero();
    }

    private Set<StudentId> seedActiveStudents(int count) {
        var ids = new LinkedHashSet<StudentId>();
        for (int i = 0; i < count; i++) {
            var student = Student.create(StudentId.generate(), DictionaryId.generate(), "Student" + i, CefrLevel.A2);
            studentRepository.save(student);
            ids.add(student.getId());
        }
        return ids;
    }

    private StudentId seedStudentWithDictionary(int wordCount) {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "Dict");
        for (int i = 0; i < wordCount; i++) {
            dictionary.addWord(WordInfo.restore(WordInfoId.generate(), new Word("word" + i), Set.of("перевод" + i),
                    PartOfSpeech.NOUN, 2, 0, 0));
        }
        dictionaryRepository.save(dictionary);

        var student = Student.create(StudentId.generate(), dictionary.getId(), "Student", CefrLevel.A2);
        studentRepository.save(student);
        return student.getId();
    }
}
