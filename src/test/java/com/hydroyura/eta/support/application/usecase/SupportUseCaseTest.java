package com.hydroyura.eta.support.application.usecase;

import com.hydroyura.eta.support.api.support.AppendSupportMessageCommand;
import com.hydroyura.eta.support.api.support.SupportAuthor;
import com.hydroyura.eta.support.api.support.SupportThreadId;
import com.hydroyura.eta.support.domain.thread.SupportThread;
import com.hydroyura.eta.support.domain.thread.SupportThreadRepository;
import com.hydroyura.eta.teacher.api.teacher.TeacherId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportUseCaseTest {

    private final SupportThreadRepository repository = mock(SupportThreadRepository.class);

    @Test
    void openShouldReuseExistingThread() {
        var teacherId = TeacherId.generate();
        var existing = SupportThread.open(SupportThreadId.generate(), teacherId, Instant.now());
        when(repository.findByTeacherId(teacherId)).thenReturn(Optional.of(existing));

        var useCase = new OpenSupportThreadUseCase(repository);

        assertThat(useCase.open(teacherId).id()).isEqualTo(existing.getId());
        verify(repository).findByTeacherId(teacherId);
    }

    @Test
    void openShouldCreateWhenAbsent() {
        var teacherId = TeacherId.generate();
        when(repository.findByTeacherId(teacherId)).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        var useCase = new OpenSupportThreadUseCase(repository);

        var opened = useCase.open(teacherId);

        assertThat(opened.teacherId()).isEqualTo(teacherId);
        verify(repository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void appendShouldAddMessageAndTouchLastMessageAt() {
        var teacherId = TeacherId.generate();
        var thread = SupportThread.open(SupportThreadId.generate(), teacherId, Instant.now());
        when(repository.findById(thread.getId())).thenReturn(Optional.of(thread));

        var useCase = new AppendSupportMessageUseCase(repository);
        useCase.append(new AppendSupportMessageCommand(thread.getId(), SupportAuthor.TEACHER, "hello"));

        assertThat(thread.getMessages()).hasSize(1);
        assertThat(thread.getMessages().get(0).author()).isEqualTo(SupportAuthor.TEACHER);
        assertThat(thread.getLastMessageAt()).isNotNull();
        verify(repository).save(thread);
    }

    @Test
    void findMessagesShouldReturnChronologicalMessages() {
        var thread = SupportThread.open(SupportThreadId.generate(), TeacherId.generate(), Instant.now());
        thread.appendMessage(SupportAuthor.TEACHER, "first", Instant.parse("2026-01-01T10:00:00Z"));
        thread.appendMessage(SupportAuthor.ADMIN, "second", Instant.parse("2026-01-01T11:00:00Z"));
        when(repository.findById(thread.getId())).thenReturn(Optional.of(thread));

        var messages = new FindSupportMessagesUseCase(repository).findByThreadId(thread.getId());

        assertThat(messages).extracting(m -> m.text()).containsExactly("first", "second");
    }

    @Test
    void findByIdShouldReturnThreadInfoWithTeacherId() {
        var teacherId = TeacherId.generate();
        var thread = SupportThread.open(SupportThreadId.generate(), teacherId, Instant.now());
        when(repository.findById(thread.getId())).thenReturn(Optional.of(thread));

        var info = new FindSupportThreadsUseCase(repository).findById(thread.getId()).orElseThrow();

        assertThat(info.teacherId()).isEqualTo(teacherId);
    }

    @Test
    void findAllShouldMapAllThreads() {
        var first = SupportThread.open(SupportThreadId.generate(), TeacherId.generate(), Instant.now());
        var second = SupportThread.open(SupportThreadId.generate(), TeacherId.generate(), Instant.now());
        when(repository.findAll()).thenReturn(List.of(first, second));

        assertThat(new FindSupportThreadsUseCase(repository).findAll()).hasSize(2);
    }
}
