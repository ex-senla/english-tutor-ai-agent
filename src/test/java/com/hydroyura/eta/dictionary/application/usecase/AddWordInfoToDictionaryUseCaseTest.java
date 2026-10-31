package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.WordFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import com.hydroyura.eta.dictionary.domain.word.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddWordInfoToDictionaryUseCaseTest {

    private AddWordToDictionaryUseCase useCase;

    private StubDictionaryRepository repository;

    @BeforeEach
    void setUp() {
        var wordFactory = new WordFactory(new WordSpecifications(
                Pattern.compile("[a-zA-Z'\\- ]+"), /* valueMinLength */ 2, /* valueMaxLength */ 50));
        var wordInfoFactory = new WordInfoFactory(new WordInfoSpecifications(
                Pattern.compile("[а-яА-ЯёЁ\\- ]+"), /* translationMinLength */ 1,
                /* translationMaxLength */ 100, /* minTranslations */ 1,
                /* maxTargetRepetitions */ 100), /* defaultTargetRepetitions */ 10);
        repository = new StubDictionaryRepository();

        useCase = new AddWordToDictionaryUseCase(repository, wordInfoFactory, wordFactory);
    }

    @Test
    void shouldAddWordToDictionary() {
        var dictId = DictionaryId.generate();
        repository.save(new Dictionary(dictId, new HashSet<>(), "My Dictionary"));

        var cmd = new AddWordCommand(dictId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN);
        useCase.execute(cmd);

        var saved = repository.findById(dictId).orElseThrow();
        assertThat(saved.getWordInfos()).hasSize(1);
        assertThat(saved.getWordInfos().iterator().next().getWord().getValue()).isEqualTo("apple");
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

    @Test
    void shouldRejectInvalidWordValue() {
        var dictId = DictionaryId.generate();
        repository.save(new Dictionary(dictId, new HashSet<>(), "My Dictionary"));

        assertThatThrownBy(() -> useCase.execute(
                new AddWordCommand(dictId, "привет", Set.of("hello"), PartOfSpeech.NOUN)))
                .isInstanceOf(com.hydroyura.eta.dictionary.domain.word.exception.WordValidationException.class);
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
