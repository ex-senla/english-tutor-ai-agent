package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.Word;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import com.hydroyura.eta.dictionary.domain.word.WordInfoFactory;
import com.hydroyura.eta.dictionary.domain.word.WordInfoSpecifications;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementWordRepetitionsUseCaseTest {

    private StubDictionaryRepository repository;

    private IncrementWordRepetitionsUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new StubDictionaryRepository();
        useCase = new IncrementWordRepetitionsUseCase(repository);
    }

    @Test
    void shouldIncrementEachFoundWordAndSkipMissing() {
        var dictId = DictionaryId.generate();
        var apple = word("apple", 5);
        var run = word("run", 9);
        var missingId = WordInfoId.generate();

        repository.save(new Dictionary(dictId, new HashSet<>(Set.of(apple, run)), "Dict"));

        useCase.execute(dictId, Set.of(apple.getId(), run.getId(), missingId));

        var saved = repository.findById(dictId).orElseThrow().getWordInfos();
        assertThat(saved).extracting(WordInfo::getCurrentRepetitions).containsExactlyInAnyOrder(6, 10);
    }

    @Test
    void shouldDoNothingForEmptySet() {
        var dictId = DictionaryId.generate();
        var apple = word("apple", 5);
        repository.save(new Dictionary(dictId, new HashSet<>(Set.of(apple)), "Dict"));

        useCase.execute(dictId, Set.of());

        var saved = repository.findById(dictId).orElseThrow().getWordInfos().stream().findFirst().orElseThrow();
        assertThat(saved.getCurrentRepetitions()).isEqualTo(5);
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
