package com.hydroyura.eta.teacher.application.demo;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoStudentsSeeder {

    private static final List<String> NAME_POOL = List.of(
            "Alex", "Maria", "John", "Emma", "Liam", "Sofia", "Noah", "Olivia", "Ethan", "Mia");

    private final DemoStudentsConfig config;

    private final CreateStudentWithDictionary createStudentWithDictionary;

    private final StudentQuery studentQuery;

    private final AddWordToDictionary addWordToDictionary;

    private final Random random;

    public DemoStudentsSeeder(
            DemoStudentsConfig config,
            CreateStudentWithDictionary createStudentWithDictionary,
            StudentQuery studentQuery,
            AddWordToDictionary addWordToDictionary,
            Random random) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.createStudentWithDictionary = Objects.requireNonNull(createStudentWithDictionary,
                "createStudentWithDictionary must not be null");
        this.studentQuery = Objects.requireNonNull(studentQuery, "studentQuery must not be null");
        this.addWordToDictionary = Objects.requireNonNull(addWordToDictionary, "addWordToDictionary must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    public void seed(TeacherId teacherId) {
        Objects.requireNonNull(teacherId, "teacherId must not be null");

        if (!config.isEnabled()) {
            log.debug("Demo students seeding is disabled");
            return;
        }

        validateConfig();

        for (int i = 0; i < config.getStudentCount(); i++) {
            seedStudent(teacherId, i + 1);
        }

        log.info("Seeded {} demo student(s) for teacher {}", config.getStudentCount(), teacherId.value());
    }

    private void validateConfig() {
        if (config.getStudentCount() < 0) {
            throw new IllegalArgumentException("studentCount must not be negative");
        }
        if (config.getMinWordsPerStudent() < 0 || config.getMaxWordsPerStudent() < config.getMinWordsPerStudent()) {
            throw new IllegalArgumentException(
                    "minWordsPerStudent must be non-negative and not exceed maxWordsPerStudent");
        }
        if (config.getMaxWordsPerStudent() > DemoWordCatalog.words().size()) {
            throw new IllegalArgumentException("maxWordsPerStudent exceeds demo word catalog size");
        }
    }

    private void seedStudent(TeacherId teacherId, int suffix) {
        var name = randomName(suffix);
        var dictionaryName = "Словарь " + name;

        var studentId = createStudentWithDictionary.execute(
                new CreateStudentWithDictionaryCommand(teacherId, name, dictionaryName));

        var dictionaryId = studentQuery.getDictionaryId(studentId)
                .orElseThrow(() -> new IllegalStateException("Dictionary not found for student " + studentId.value()));

        seedWords(dictionaryId);

        log.info("Demo student '{}' created with dictionary {}", name, dictionaryId.value());
    }

    private String randomName(int suffix) {
        var base = NAME_POOL.get(random.nextInt(NAME_POOL.size()));
        return base + " " + suffix;
    }

    private void seedWords(DictionaryId dictionaryId) {
        var min = config.getMinWordsPerStudent();
        var max = config.getMaxWordsPerStudent();
        var count = min + random.nextInt(max - min + 1);

        var words = DemoWordCatalog.words();
        var indices = new ArrayList<>(IntStream.range(0, words.size()).boxed().toList());
        Collections.shuffle(indices, random);

        for (int i = 0; i < count; i++) {
            var word = words.get(indices.get(i));
            addWordToDictionary.execute(
                    new AddWordCommand(dictionaryId, word.value(), word.translations(), word.partOfSpeech()));
        }
    }
}
