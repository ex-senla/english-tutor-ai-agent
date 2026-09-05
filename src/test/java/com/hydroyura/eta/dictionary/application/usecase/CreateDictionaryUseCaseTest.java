package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.CreateDictionaryCommand;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDictionaryUseCaseTest {

    private CreateDictionaryUseCase useCase;

    private StubDictionaryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubDictionaryRepository();
        useCase = new CreateDictionaryUseCase(repository);
    }

    @Test
    void shouldCreateDictionary() {
        var id = useCase.execute(new CreateDictionaryCommand("My Dictionary"));

        assertThat(id).isNotNull();
        var saved = repository.findById(id).orElseThrow();
        assertThat(saved.getName()).isEqualTo("My Dictionary");
        assertThat(saved.getWords()).isEmpty();
    }

    // --- stub ---

    static class StubDictionaryRepository implements DictionaryRepository {

        private final Map<DictionaryId, Dictionary> store = new HashMap<>();

        @Override
        public Dictionary save(Dictionary dict) {
            store.put(dict.getId(), dict);
            return dict;
        }

        @Override
        public Optional<Dictionary> findById(DictionaryId id) {
            return Optional.ofNullable(store.get(id));
        }
    }
}
