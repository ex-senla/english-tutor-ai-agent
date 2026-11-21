package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.SearchStudentsQuery;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.domain.student.StudentRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Контрактный тест read-модели против {@code JpaStudentRepository} (Testcontainers):
 * удалённые ученики исключаются из всех read-путей, включая {@code existsByName}.
 */
@Transactional
class StudentQueryServiceDeletedContractTest extends PostgresIntegrationTestBase {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentQuery studentQuery;

    @Test
    void shouldExcludeDeletedFromFindStudentsByIds() {
        var deleted = saveDeleted("Anna");
        var alive = saveAlive("Bob");

        var result = studentQuery.findStudentsByIds(Set.of(deleted, alive));

        assertThat(result).extracting(StudentInfo::id).containsExactly(alive);
    }

    @Test
    void shouldExcludeDeletedFromSearchByNameFragment() {
        var deleted = saveDeleted("Anna");
        var alive = saveAlive("Joan");

        var result = studentQuery.searchByNameFragment(new SearchStudentsQuery(Set.of(deleted, alive), "an"));

        assertThat(result).extracting(StudentInfo::name).containsExactly("Joan");
    }

    @Test
    void shouldReturnEmptyForDeletedNameInFindByNameIn() {
        var deleted = saveDeleted("Anna");

        assertThat(studentQuery.findByNameIn(new FindStudentByNameQuery(Set.of(deleted), "Anna"))).isEmpty();
    }

    @Test
    void shouldReturnFalseForDeletedNameInExistsByName() {
        var deleted = saveDeleted("Anna");

        assertThat(studentQuery.existsByName(new StudentExistsByNameQuery(Set.of(deleted), "Anna"))).isFalse();
    }

    @Test
    void shouldReturnEmptyDetailsForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(studentQuery.findStudentDetails(deleted)).isEmpty();
    }

    @Test
    void shouldReturnEmptyCefrLevelForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(studentQuery.getCefrLevel(deleted)).isEmpty();
    }

    @Test
    void shouldReturnEmptyDictionaryIdForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(studentQuery.getDictionaryId(deleted)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForAllReadPathsForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(studentQuery.findStudentsByIds(Set.of(deleted))).isEmpty();
        assertThat(studentQuery.searchByNameFragment(new SearchStudentsQuery(Set.of(deleted), "Ann"))).isEmpty();
        assertThat(studentQuery.findByNameIn(new FindStudentByNameQuery(Set.of(deleted), "Anna"))).isEmpty();
        assertThat(studentQuery.existsByName(new StudentExistsByNameQuery(Set.of(deleted), "Anna"))).isFalse();
        assertThat(studentQuery.findStudentDetails(deleted)).isEmpty();
        assertThat(studentQuery.getCefrLevel(deleted)).isEmpty();
        assertThat(studentQuery.getDictionaryId(deleted)).isEmpty();
    }

    private StudentId saveAlive(String name) {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), name, CefrLevel.B1);
        studentRepository.save(student);
        return student.getId();
    }

    private StudentId saveDeleted(String name) {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), name, CefrLevel.B1);
        student.markDeleted();
        studentRepository.save(student);
        return student.getId();
    }
}
