package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.DictionaryNotFoundException;
import com.hydroyura.eta.dictionary.domain.word.Word;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import com.hydroyura.eta.dictionary.domain.word.exception.WordNotFoundException;
import com.hydroyura.eta.dictionary.domain.word.exception.WordResetNotAllowedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResetWordLearningUseCaseTest {

    private StubDictionaryRepository repository;

    private ResetWordLearningUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new StubDictionaryRepository();
        useCase = new ResetWordLearningUseCase(repository);
    }

    @Test
    void shouldResetLearnedWord() {
        var dictId = DictionaryId.generate();
        var wordInfo = word("apple", 10);
        repository.save(new Dictionary(dictId, new HashSet<>(Set.of(wordInfo)), "Dict"));

        useCase.execute(dictId, wordInfo.getId());

        var saved = repository.findById(dictId).orElseThrow().getWordInfos().stream().findFirst().orElseThrow();
        assertThat(saved.getCurrentRepetitions()).isZero();
        assertThat(saved.getResetCount()).isEqualTo(1);
    }

    @Test
    void shouldRejectResetForNotLearnedWordWithoutMutation() {
        var dictId = DictionaryId.generate();
        var wordInfo = word("apple", 5);
        repository.save(new Dictionary(dictId, new HashSet<>(Set.of(wordInfo)), "Dict"));

        assertThatThrownBy(() -> useCase.execute(dictId, wordInfo.getId()))
                .isInstanceOf(WordResetNotAllowedException.class);

        var saved = repository.findById(dictId).orElseThrow().getWordInfos().stream().findFirst().orElseThrow();
        assertThat(saved.getCurrentRepetitions()).isEqualTo(5);
        assertThat(saved.getResetCount()).isZero();
    }

    @Test
    void shouldThrowWhenWordNotFound() {
        var dictId = DictionaryId.generate();
        repository.save(new Dictionary(dictId, new HashSet<>(), "Dict"));

        assertThatThrownBy(() -> useCase.execute(dictId, WordInfoId.generate()))
                .isInstanceOf(WordNotFoundException.class);
    }

    @Test
    void shouldThrowWhenDictionaryNotFound() {
        assertThatThrownBy(() -> useCase.execute(DictionaryId.generate(), WordInfoId.generate()))
                .isInstanceOf(DictionaryNotFoundException.class);
    }

    private static WordInfo word(String value, int currentRepetitions) {
        var specs = new WordInfoSpecifications(
                Pattern.compile("[а-яА-ЯёЁ\\- ]+"),
                /* translationMinLength */ 1,
                /* translationMaxLength */ 100,
                /* minTranslations */ 1,
                /* maxTargetRepetitions */ 100);
        var factory = new WordInfoFactory(specs, /* defaultTargetRepetitions */ 10);
        var wordInfo = factory.create(new Word(value), Set.of("перевод"), PartOfSpeech.NOUN);
        for (var i = 0; i < currentRepetitions; i++) {
            wordInfo.incrementRepetition();
        }
        return wordInfo;
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
