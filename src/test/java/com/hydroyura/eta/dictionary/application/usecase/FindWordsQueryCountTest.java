package com.hydroyura.eta.dictionary.application.usecase;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.dictionary.FindWords;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import com.hydroyura.eta.dictionary.domain.word.Word;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FindWordsQueryCountTest extends PostgresIntegrationTestBase {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private FindWords findWords;

    @Test
    void shouldUseConstantQueriesForFindByDictionaryId() {
        var small = seedDictionary(10);
        var large = seedDictionary(40);

        clearSession();
        var smallWindow = measureWindow(() -> findWords.findByDictionaryId(small));
        clearSession();
        var largeWindow = measureWindow(() -> findWords.findByDictionaryId(large));

        assertSameQueryCount(smallWindow, largeWindow, 2);
    }

    @Test
    void shouldUseConstantQueriesForGetStats() {
        var small = seedDictionary(10);
        var large = seedDictionary(40);

        clearSession();
        var smallWindow = measureWindow(() -> findWords.getStats(small));
        clearSession();
        var largeWindow = measureWindow(() -> findWords.getStats(large));

        assertSameQueryCount(smallWindow, largeWindow, 2);
    }

    private void assertSameQueryCount(QueryWindow small, QueryWindow large, long ceiling) {
        assertThat(large.prepare()).isEqualTo(small.prepare());
        assertThat(large.queryExecution()).isEqualTo(small.queryExecution());
        assertThat(small.prepare()).isLessThanOrEqualTo(ceiling);
        assertThat(small.lazyFetches()).isZero();
        assertThat(large.lazyFetches()).isZero();
    }

    private DictionaryId seedDictionary(int wordCount) {
        var dictionary = new Dictionary(DictionaryId.generate(), new HashSet<>(), "Dict");
        for (int i = 0; i < wordCount; i++) {
            dictionary.addWord(WordInfo.restore(WordInfoId.generate(), new Word("word" + i),
                    Set.of("перевод" + i, "перевод-" + i), PartOfSpeech.NOUN, 2, 0, 0));
        }
        dictionaryRepository.save(dictionary);
        return dictionary.getId();
    }
}
