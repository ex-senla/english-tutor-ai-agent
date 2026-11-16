package com.hydroyura.eta.student.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryStats;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.student.api.lesson.FindActiveLesson;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.SearchStudentsQuery;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.domain.student.Student;
import com.hydroyura.eta.student.infrastructure.persistence.InMemoryStudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Контрактный тест read-модели на реальном {@link InMemoryStudentRepository}: фильтр удалённых
 * проверяется в том числе для {@code existsByName} (репозиторий), а не только в {@link StudentQueryService}.
 */
class StudentQueryServiceDeletedContractTest {

    private StudentQueryService service;

    private InMemoryStudentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryStudentRepository();
        var findWords = mock(FindWords.class);
        var findActiveLesson = mock(FindActiveLesson.class);
        when(findWords.getStats(any())).thenReturn(new DictionaryStats(0, 0, 0, 0));
        when(findActiveLesson.findByStudentId(any())).thenReturn(Optional.empty());
        service = new StudentQueryService(repository, findWords, findActiveLesson);
    }

    @Test
    void shouldExcludeDeletedFromFindStudentsByIds() {
        var deleted = saveDeleted("Anna");
        var alive = saveAlive("Bob");

        var result = service.findStudentsByIds(Set.of(deleted, alive));

        assertThat(result).extracting(StudentInfo::id).containsExactly(alive);
    }

    @Test
    void shouldExcludeDeletedFromSearchByNameFragment() {
        var deleted = saveDeleted("Anna");
        var alive = saveAlive("Joan");

        var result = service.searchByNameFragment(new SearchStudentsQuery(Set.of(deleted, alive), "an"));

        assertThat(result).extracting(StudentInfo::name).containsExactly("Joan");
    }

    @Test
    void shouldReturnEmptyForDeletedNameInFindByNameIn() {
        var deleted = saveDeleted("Anna");

        assertThat(service.findByNameIn(new FindStudentByNameQuery(Set.of(deleted), "Anna"))).isEmpty();
    }

    @Test
    void shouldReturnFalseForDeletedNameInExistsByName() {
        var deleted = saveDeleted("Anna");

        assertThat(service.existsByName(new StudentExistsByNameQuery(Set.of(deleted), "Anna"))).isFalse();
    }

    @Test
    void shouldReturnEmptyDetailsForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(service.findStudentDetails(deleted)).isEmpty();
    }

    @Test
    void shouldReturnEmptyCefrLevelForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(service.getCefrLevel(deleted)).isEmpty();
    }

    @Test
    void shouldReturnEmptyDictionaryIdForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(service.getDictionaryId(deleted)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForAllReadPathsForDeleted() {
        var deleted = saveDeleted("Anna");

        assertThat(service.findStudentsByIds(Set.of(deleted))).isEmpty();
        assertThat(service.searchByNameFragment(new SearchStudentsQuery(Set.of(deleted), "Ann"))).isEmpty();
        assertThat(service.findByNameIn(new FindStudentByNameQuery(Set.of(deleted), "Anna"))).isEmpty();
        assertThat(service.existsByName(new StudentExistsByNameQuery(Set.of(deleted), "Anna"))).isFalse();
        assertThat(service.findStudentDetails(deleted)).isEmpty();
        assertThat(service.getCefrLevel(deleted)).isEmpty();
        assertThat(service.getDictionaryId(deleted)).isEmpty();
    }

    private StudentId saveAlive(String name) {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), name, CefrLevel.B1);
        repository.save(student);
        return student.getId();
    }

    private StudentId saveDeleted(String name) {
        var student = Student.create(StudentId.generate(), DictionaryId.generate(), name, CefrLevel.B1);
        student.markDeleted();
        repository.save(student);
        return student.getId();
    }
}
