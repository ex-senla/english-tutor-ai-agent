package com.hydroyura.eta.chatbot.infrastructure.persistence;

import com.hydroyura.eta.PostgresIntegrationTestBase;
import com.hydroyura.eta.chatbot.application.ChatService;
import com.hydroyura.eta.chatbot.domain.chat.Chat;
import com.hydroyura.eta.chatbot.domain.chat.ChatId;
import com.hydroyura.eta.chatbot.domain.chat.ChatRepository;
import com.hydroyura.eta.chatbot.domain.chat.ChatState;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPersistenceRoundTripTest extends PostgresIntegrationTestBase {

    @Autowired
    private ChatRepository repository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRoundTripChatWithTypedContext() {
        var chat = Chat.ofDefaults(new ChatId(1L));
        chat.updateState(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        chat.getContext().put("wordSelection", Set.of("apple", "run"));
        chat.getContext().put("exerciseType", ExerciseType.MULTIPLE_CHOICE);
        chat.getContext().put("exerciseTopic", new CatalogTopic("present-simple"));
        chat.getContext().put("exerciseId", new ExerciseId(UUID.randomUUID()));
        chat.getContext().put("wordPos", PartOfSpeech.NOUN);
        chat.getContext().put("wordTranslations", List.of("яблоко"));
        chat.getContext().put("topicPage", 3);
        repository.save(chat);

        var loaded = repository.findById(new ChatId(1L)).orElseThrow();
        assertThat(loaded.getState()).isEqualTo(ChatState.AWAITING_EXERCISE_WORD_SELECTION);
        assertThat((Set<String>) loaded.getContext().get("wordSelection")).containsExactlyInAnyOrder("apple", "run");
        assertThat(loaded.getContext().get("exerciseType")).isEqualTo(ExerciseType.MULTIPLE_CHOICE);
        assertThat(loaded.getContext().get("exerciseTopic")).isEqualTo(new CatalogTopic("present-simple"));
        assertThat(loaded.getContext().get("wordPos")).isEqualTo(PartOfSpeech.NOUN);
        assertThat((List<String>) loaded.getContext().get("wordTranslations")).containsExactly("яблоко");
        assertThat(loaded.getContext().get("topicPage")).isEqualTo(3);
    }

    @Test
    void shouldRoundTripEmptyContext() {
        var chat = Chat.ofDefaults(new ChatId(2L));
        repository.save(chat);

        var loaded = repository.findById(new ChatId(2L)).orElseThrow();
        assertThat(loaded.getState()).isEqualTo(ChatState.INITIAL);
        assertThat(loaded.getContext()).isEmpty();
    }

    @Test
    void shouldOverwriteExistingKeys() {
        var chat = Chat.ofDefaults(new ChatId(3L));
        chat.getContext().put("key", "first");
        repository.save(chat);

        chat.getContext().put("key", "second");
        repository.save(chat);

        var loaded = repository.findById(new ChatId(3L)).orElseThrow();
        assertThat(loaded.getContext().get("key")).isEqualTo("second");
    }

    @Test
    void shouldNotCreateRowOnPlainFindById() {
        var count = jdbcTemplate.queryForObject("SELECT count(*) FROM chat WHERE chat_id = 999", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void shouldCreateChatExactlyOnceViaChatService() {
        var first = chatService.getOrCreate(10L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM chat WHERE chat_id = 10", Integer.class))
                .isZero();

        chatService.save(first);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM chat WHERE chat_id = 10", Integer.class))
                .isEqualTo(1);

        chatService.save(chatService.getOrCreate(10L));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM chat WHERE chat_id = 10", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void shouldSkipNullContextValue() {
        var chat = Chat.ofDefaults(new ChatId(4L));
        chat.getContext().put("topicMatchedSlug", null);
        chat.getContext().put("topicCanonicalName", "Past Simple");
        repository.save(chat);

        var loaded = repository.findById(new ChatId(4L)).orElseThrow();
        assertThat(loaded.getContext()).doesNotContainKey("topicMatchedSlug");
        assertThat(loaded.getContext().get("topicMatchedSlug")).isNull();
        assertThat(loaded.getContext().get("topicCanonicalName")).isEqualTo("Past Simple");
    }
}
