package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordSpecificationConfig;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddWordToDictionaryUseCaseTest {

    private AddWordToDictionaryUseCase useCase;

    private StubDictionaryRepository repository;

    @BeforeEach
    void setUp() {
        var config = new WordSpecificationConfig() {

            @Override
            public String valueAllowedPattern() {
                return "[a-zA-Z'\\- ]+";
            }

            @Override
            public int valueMinLength() {
                return 2;
            }

            @Override
            public int valueMaxLength() {
                return 50;
            }

            @Override
            public String translationAllowedPattern() {
                return "[а-яА-ЯёЁ ]+";
            }

            @Override
            public int translationMinLength() {
                return 1;
            }

            @Override
            public int translationMaxLength() {
                return 100;
            }

            @Override
            public int minTranslations() {
                return 1;
            }

            @Override
            public int maxTargetRepetitions() {
                return 100;
            }

            @Override
            public int defaultTargetRepetitions() {
                return 10;
            }
        };
        var specs = new WordSpecifications(config);
        var factory = new WordFactory(specs, config);
        repository = new StubDictionaryRepository();

        useCase = new AddWordToDictionaryUseCase(repository, factory);
    }

    @Test
    void shouldAddWordToDictionary() {
        var dictId = DictionaryId.generate();
        repository.save(new Dictionary(dictId, new HashSet<>(), "My Dictionary"));

        var cmd = new AddWordCommand(dictId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN);
        useCase.execute(cmd);

        var saved = repository.findById(dictId).orElseThrow();
        assertThat(saved.getWords()).hasSize(1);
        assertThat(saved.getWords().iterator().next().getValue()).isEqualTo("apple");
    }

    @Test
    void shouldThrowWhenDictionaryNotFound() {
        var cmd = new AddWordCommand(DictionaryId.generate(), "apple", Set.of("яблоко"), PartOfSpeech.NOUN);

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(DictionaryNotFoundException.class);
    }

    @Test
    void shouldThrowOnDuplicateWord() {
        var dictId = DictionaryId.generate();
        repository.save(new Dictionary(dictId, new HashSet<>(), "My Dictionary"));

        useCase.execute(new AddWordCommand(dictId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN));

        assertThatThrownBy(() -> useCase.execute(new AddWordCommand(dictId, "apple", Set.of("яблочко"),
                PartOfSpeech.NOUN)))
                .isInstanceOf(WordAlreadyExistsException.class);
    }

    // --- stub ---

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
