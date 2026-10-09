package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.student.api.student.FindStudentByNameQuery;
import com.hydroyura.eta.student.api.student.StudentDetails;
import com.hydroyura.eta.student.api.student.StudentExistsByNameQuery;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentInfo;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoStudentsSeederTest {

    private DemoStudentsSeeder seeder;

    private StubConfig config;

    private final Map<StudentId, DictionaryId> studentDictionaries = new HashMap<>();

    private final Map<DictionaryId, List<DemoWord>> addedWords = new HashMap<>();

    private final List<String> createdStudentNames = new ArrayList<>();

    private TeacherId seededTeacherId;

    @BeforeEach
    void setUp() {
        config = new StubConfig();

        CreateStudentWithDictionary createStudentWithDictionary = cmd -> {
            seededTeacherId = cmd.teacherId();
            var studentId = StudentId.generate();
            var dictionaryId = DictionaryId.generate();
            studentDictionaries.put(studentId, dictionaryId);
            createdStudentNames.add(cmd.studentName());
            return studentId;
        };

        StudentQuery studentQuery = new StudentQuery() {

            @Override
            public boolean existsByName(StudentExistsByNameQuery query) {
                return false;
            }

            @Override
            public Optional<StudentId> findByNameIn(FindStudentByNameQuery query) {
                return Optional.empty();
            }

            @Override
            public Optional<DictionaryId> getDictionaryId(StudentId studentId) {
                return Optional.ofNullable(studentDictionaries.get(studentId));
            }

            @Override
            public List<StudentInfo> findStudentsByIds(Set<StudentId> ids) {
                return List.of();
            }

            @Override
            public Optional<StudentDetails> findStudentDetails(StudentId studentId) {
                return Optional.empty();
            }
        };

        AddWordToDictionary addWordToDictionary = cmd -> {
            addedWords.computeIfAbsent(cmd.dictionaryId(), k -> new ArrayList<>())
                    .add(new DemoWord(cmd.value(), cmd.translations(), cmd.partOfSpeech()));
            return WordId.generate();
        };

        seeder = new DemoStudentsSeeder(config, createStudentWithDictionary, studentQuery, addWordToDictionary,
                new Random(42));
    }

    @Test
    void shouldDoNothingWhenDisabled() {
        config.enabled = false;

        seeder.seed(TeacherId.generate());

        assertThat(createdStudentNames).isEmpty();
        assertThat(addedWords).isEmpty();
    }

    @Test
    void shouldCreateConfiguredNumberOfStudentsWithUniqueNames() {
        config.enabled = true;
        config.studentCount = 4;
        config.minWordsPerStudent = 1;
        config.maxWordsPerStudent = 1;
        var teacherId = TeacherId.generate();

        seeder.seed(teacherId);

        assertThat(createdStudentNames).hasSize(4);
        assertThat(createdStudentNames).allSatisfy(name -> assertThat(name).isNotBlank());
        assertThat(new HashSet<>(createdStudentNames)).hasSize(4);
        assertThat(seededTeacherId).isEqualTo(teacherId);
    }

    @Test
    void shouldSeedWordsWithinConfiguredRange() {
        config.enabled = true;
        config.studentCount = 2;
        config.minWordsPerStudent = 3;
        config.maxWordsPerStudent = 5;

        seeder.seed(TeacherId.generate());

        assertThat(addedWords.values()).hasSize(2);
        assertThat(addedWords.values())
                .allSatisfy(words -> assertThat(words).hasSizeBetween(3, 5));
    }

    @Test
    void shouldRejectNegativeStudentCount() {
        config.enabled = true;
        config.studentCount = -1;

        assertThatThrownBy(() -> seeder.seed(TeacherId.generate()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentCount");
    }

    @Test
    void shouldRejectMinWordsGreaterThanMax() {
        config.enabled = true;
        config.studentCount = 1;
        config.minWordsPerStudent = 6;
        config.maxWordsPerStudent = 2;

        assertThatThrownBy(() -> seeder.seed(TeacherId.generate()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minWordsPerStudent");
    }

    @Test
    void shouldNotCreateStudentsWhenCountIsZero() {
        config.enabled = true;
        config.studentCount = 0;

        seeder.seed(TeacherId.generate());

        assertThat(createdStudentNames).isEmpty();
        assertThat(addedWords).isEmpty();
    }

    static class StubConfig implements DemoStudentsConfig {

        boolean enabled;

        int studentCount;

        int minWordsPerStudent;

        int maxWordsPerStudent;

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int getStudentCount() {
            return studentCount;
        }

        @Override
        public int getMinWordsPerStudent() {
            return minWordsPerStudent;
        }

        @Override
        public int getMaxWordsPerStudent() {
            return maxWordsPerStudent;
        }
    }
}
