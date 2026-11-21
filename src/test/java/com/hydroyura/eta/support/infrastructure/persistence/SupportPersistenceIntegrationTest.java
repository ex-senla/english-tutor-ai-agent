package com.hydroyura.eta.support.infrastructure.persistence;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.domain.thread.SupportThread;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportPersistenceIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private SupportThreadRepository repository;

    @Test
    void shouldRoundTripThreadWithMessages() {
        var thread = SupportThread.open(SupportThreadId.generate(), TeacherId.generate(), Instant.now());
        thread.appendMessage(SupportAuthor.TEACHER, "hello", Instant.now());
        repository.save(thread);

        var loaded = repository.findById(thread.getId()).orElseThrow();

        assertThat(loaded.getTeacherId()).isEqualTo(thread.getTeacherId());
        assertThat(loaded.getMessages()).hasSize(1);
        assertThat(loaded.getMessages().get(0).text()).isEqualTo("hello");
    }

    @Test
    void shouldRejectSecondOpenThreadForSameTeacher() {
        var teacherId = TeacherId.generate();
        repository.save(SupportThread.open(SupportThreadId.generate(), teacherId, Instant.now()));

        assertThatThrownBy(() -> repository.save(
                SupportThread.open(SupportThreadId.generate(), teacherId, Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSortThreadsByCoalesceLastMessageAtAndCreatedAt() {
        var base = Instant.parse("2026-01-01T10:00:00Z");
        var withMessage = SupportThread.open(SupportThreadId.generate(), TeacherId.generate(), base);
        withMessage.appendMessage(SupportAuthor.TEACHER, "old", base.plusSeconds(60));
        repository.save(withMessage);

        var noMessage = SupportThread.open(SupportThreadId.generate(), TeacherId.generate(), base.plusSeconds(120));
        repository.save(noMessage);

        var threads = repository.findAll();

        assertThat(threads).extracting(t -> t.getId().value())
                .containsSubsequence(noMessage.getId().value(), withMessage.getId().value());
    }
}
