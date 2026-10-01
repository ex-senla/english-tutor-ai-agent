package com.hydroyura.eta.dictionary.domain.word;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordId;
import com.hydroyura.eta.dictionary.api.word.WordStatus;
import com.hydroyura.eta.dictionary.application.usecase.FindWordsService;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FindWordsServiceTest {

    private FindWordsService service;

    private StubDictionaryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubDictionaryRepository();
        service = new FindWordsService(repository);
    }

    @Test
    void shouldMapWordStatusToProjection() {
        var dictId = DictionaryId.generate();
        var words = new HashSet<Word>();
        words.add(new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0));
        words.add(new Word(WordId.generate(), "run", Set.of("бежать"), PartOfSpeech.VERB, 10, 5));
        words.add(new Word(WordId.generate(), "big", Set.of("большой"), PartOfSpeech.ADJECTIVE, 10, 10));
        repository.save(new Dictionary(dictId, words, "My Dictionary"));

        var projections = service.findByDictionaryId(dictId);

        assertThat(projections).hasSize(3);
        assertThat(projections).extracting(projection -> projection.status())
                .containsExactlyInAnyOrder(WordStatus.NEW, WordStatus.IN_PROGRESS, WordStatus.LEARNED);
    }

    @Test
    void shouldCountStatsByStatus() {
        var dictId = DictionaryId.generate();
        var words = new HashSet<Word>();
        words.add(new Word(WordId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN, 10, 0));
        words.add(new Word(WordId.generate(), "run", Set.of("бежать"), PartOfSpeech.VERB, 10, 5));
        words.add(new Word(WordId.generate(), "big", Set.of("большой"), PartOfSpeech.ADJECTIVE, 10, 10));
        repository.save(new Dictionary(dictId, words, "My Dictionary"));

        var stats = service.getStats(dictId);

        assertThat(stats.totalWords()).isEqualTo(3);
        assertThat(stats.newCount()).isEqualTo(1);
        assertThat(stats.inProgressCount()).isEqualTo(1);
        assertThat(stats.learnedCount()).isEqualTo(1);
    }

    static class StubDictionaryRepository implements DictionaryRepository {

        private final Map<DictionaryId, Dictionary> store = new HashMap<>();

        @Override
        public Dictionary save(Dictionary dictionary) {
            store.put(dictionary.getId(), dictionary);
            return dictionary;
        }

        @Override
        public Optional<Dictionary> findById(DictionaryId id) {
            return Optional.ofNullable(store.get(id));
        }
    }
}
