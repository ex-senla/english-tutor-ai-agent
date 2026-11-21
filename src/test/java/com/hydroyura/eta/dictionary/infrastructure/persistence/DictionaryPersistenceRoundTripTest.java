package com.hydroyura.eta.dictionary.infrastructure.persistence;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.dictionary.exception.WordAlreadyExistsException;
import com.hydroyura.eta.dictionary.domain.word.Word;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DictionaryPersistenceRoundTripTest extends PostgresIntegrationTestBase {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void shouldRoundTripDictionaryWithWords() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "My Dictionary");
        dictionary.addWord(word("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 3, 1, 0));
        dictionary.addWord(word("run", Set.of("бежать", "бег"), PartOfSpeech.VERB, 2, 0, 2));

        repository.save(dictionary);

        var loaded = repository.findById(dictionary.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("My Dictionary");
        assertThat(loaded.getWordInfos()).hasSize(2);
        assertThat(loaded.getWordInfos())
                .extracting(w -> w.getWord().value())
                .containsExactlyInAnyOrder("apple", "run");

        var apple = loaded.getWordInfos().stream()
                .filter(w -> w.getWord().value().equals("apple"))
                .findFirst().orElseThrow();
        assertThat(apple.getTranslations()).containsExactly("яблоко");
        assertThat(apple.getPartOfSpeech()).isEqualTo(PartOfSpeech.NOUN);
        assertThat(apple.getTargetRepetitions()).isEqualTo(3);
        assertThat(apple.getCurrentRepetitions()).isEqualTo(1);
        assertThat(apple.getResetCount()).isZero();
    }

    @Test
    void shouldRejectDuplicateWordCaseInsensitive() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "Dict");
        dictionary.addWord(word("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 2, 0, 0));

        assertThatThrownBy(() -> dictionary.addWord(
                word("Apple", Set.of("яблоко"), PartOfSpeech.NOUN, 2, 0, 0)))
                .isInstanceOf(WordAlreadyExistsException.class);
    }

    @Test
    void shouldUpdateCountersWithoutDuplicatingWordsOnResave() {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "Dict");
        dictionary.addWord(word("apple", Set.of("яблоко"), PartOfSpeech.NOUN, 2, 0, 0));
        repository.save(dictionary);

        var apple = dictionary.getWordInfos().iterator().next();
        apple.incrementRepetition();
        repository.save(dictionary);

        var loaded = repository.findById(dictionary.getId()).orElseThrow();
        assertThat(loaded.getWordInfos()).hasSize(1);
        assertThat(loaded.getWordInfos().iterator().next().getCurrentRepetitions()).isEqualTo(1);
    }

    private WordInfo word(String value, Set<String> translations, PartOfSpeech pos, int target, int current,
            int resetCount) {
        return WordInfo.restore(WordInfoId.generate(), new Word(value), translations, pos, target, current, resetCount);
    }
}
