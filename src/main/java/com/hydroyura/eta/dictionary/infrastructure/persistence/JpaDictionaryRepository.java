package com.hydroyura.eta.dictionary.infrastructure.persistence;

import com.hydroyura.eta.dictionary.api.dictionary.DictionaryId;
import com.hydroyura.eta.dictionary.domain.dictionary.Dictionary;
import com.hydroyura.eta.dictionary.domain.dictionary.DictionaryRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaDictionaryRepository implements DictionaryRepository {

    private final DictionaryDataRepository dataRepository;

    private final DictionaryJpaMapper mapper = new DictionaryJpaMapper();

    private final EntityManager entityManager;

    public JpaDictionaryRepository(DictionaryDataRepository dataRepository, EntityManager entityManager) {
        this.dataRepository = dataRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Dictionary save(Dictionary dictionary) {
        var id = dictionary.getId().value();
        var existing = dataRepository.findWithWordInfos(id);
        if (existing.isEmpty()) {
            var entity = mapper.toEntity(dictionary);
            mapper.sync(entity, dictionary);
            entityManager.persist(entity);
        } else {
            mapper.sync(existing.get(), dictionary);
        }
        return dictionary;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dictionary> findById(DictionaryId id) {
        return dataRepository.findWithWordInfos(id.value()).map(mapper::toDomain);
    }
}
