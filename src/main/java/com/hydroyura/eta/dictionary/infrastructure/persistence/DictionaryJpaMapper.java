package com.hydroyura.eta.dictionary.infrastructure.persistence;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.word.Word;
import com.hydroyura.eta.dictionary.domain.word.WordInfo;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

final class DictionaryJpaMapper {

    DictionaryJpa toEntity(Dictionary domain) {
        var entity = new DictionaryJpa();
        entity.id = domain.getId().value();
        entity.name = domain.getName();
        return entity;
    }

    void sync(DictionaryJpa entity, Dictionary domain) {
        entity.name = domain.getName();
        syncWordInfos(entity, domain);
    }

    Dictionary toDomain(DictionaryJpa entity) {
        var wordInfos = entity.getWordInfos().stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
        return new Dictionary(new DictionaryId(entity.getId()), wordInfos, entity.getName());
    }

    private void syncWordInfos(DictionaryJpa entity, Dictionary domain) {
        var byId = entity.getWordInfos().stream()
                .collect(Collectors.toMap(WordInfoJpa::getId, Function.identity()));
        var domainIds = domain.getWordInfos().stream()
                .map(word -> word.getId().value())
                .collect(Collectors.toSet());

        entity.getWordInfos().removeIf(word -> !domainIds.contains(word.getId()));

        for (var word : domain.getWordInfos()) {
            var jpa = byId.get(word.getId().value());
            if (jpa == null) {
                jpa = new WordInfoJpa();
                jpa.id = word.getId().value();
                jpa.dictionary = entity;
                entity.getWordInfos().add(jpa);
            }
            syncWord(jpa, word);
        }
    }

    private void syncWord(WordInfoJpa jpa, WordInfo word) {
        jpa.wordValue = word.getWord().value();
        jpa.partOfSpeech = word.getPartOfSpeech();
        jpa.targetRepetitions = word.getTargetRepetitions();
        jpa.currentRepetitions = word.getCurrentRepetitions();
        jpa.resetCount = word.getResetCount();
        jpa.getTranslations().clear();
        jpa.getTranslations().addAll(word.getTranslations());
    }

    private WordInfo toDomain(WordInfoJpa entity) {
        var word = new Word(entity.getWordValue());
        return WordInfo.restore(
                new WordInfoId(entity.getId()),
                word,
                Set.copyOf(entity.getTranslations()),
                entity.getPartOfSpeech(),
                entity.getTargetRepetitions(),
                entity.getCurrentRepetitions(),
                entity.getResetCount());
    }
}
