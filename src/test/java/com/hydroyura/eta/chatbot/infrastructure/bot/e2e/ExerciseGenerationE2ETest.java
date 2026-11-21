package com.hydroyura.eta.chatbot.infrastructure.bot.e2e;

import com.hydroyura.eta.dictionary.api.dictionary.AddWordCommand;
import com.hydroyura.eta.dictionary.api.dictionary.AddWordToDictionary;
import com.hydroyura.eta.dictionary.api.word.PartOfSpeech;
import com.hydroyura.eta.dictionary.api.word.WordInfoId;
import com.hydroyura.eta.exercise.api.exercise.CatalogTopic;
import com.hydroyura.eta.exercise.api.exercise.ExerciseDto;
import com.hydroyura.eta.exercise.api.exercise.ExerciseId;
import com.hydroyura.eta.exercise.api.exercise.ExerciseItem;
import com.hydroyura.eta.exercise.api.exercise.ExerciseType;
import com.hydroyura.eta.exercise.api.exercise.GenerateExerciseCommand;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.student.api.student.CefrLevel;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionary;
import com.hydroyura.eta.teacher.api.teacher.CreateStudentWithDictionaryCommand;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExerciseGenerationE2ETest extends BotE2eTestBase {

    @Autowired
    private CreateStudentWithDictionary createStudentWithDictionary;

    @Autowired
    private AddWordToDictionary addWordToDictionary;

    @Autowired
    private FindTeacher findTeacher;

    @Autowired
    private StudentQuery studentQuery;

    private String studentId;

    private String wordInfoId;

    @BeforeEach
    void seedDictionary() {
        botTester.command("/register");
        botTester.text("Иван");

        var teacherId = findTeacher.findByTelegramChatId(CHAT_ID).orElseThrow();
        studentId = createStudentWithDictionary.execute(
                new CreateStudentWithDictionaryCommand(teacherId, "Иван", "Словарь Иван", CefrLevel.B1))
                .value().toString();

        var dictionaryId = studentQuery.getDictionaryId(new StudentId(UUID.fromString(studentId))).orElseThrow();
        var wordId = addWordToDictionary.execute(
                new AddWordCommand(dictionaryId, "apple", Set.of("яблоко"), PartOfSpeech.NOUN));
        wordInfoId = wordId.value().toString();

        botTester.reset();
    }

    @Test
    void shouldGenerateExerciseWithPdfs() {
        when(exerciseGenerator.generate(any(GenerateExerciseCommand.class), any()))
                .thenReturn(exerciseDto());

        botTester.command("/list");
        botTester.callback("student:" + studentId);
        assertThat(botTester.chatState()).isEqualTo("STUDENT_OPTIONS");

        botTester.callback("action:exercise");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_EXERCISE_TYPE");

        botTester.callback("exercise:fillintheblank");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_EXERCISE_TOPIC");

        botTester.callback("topic:present-simple");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_EXERCISE_THEMATIC_TOPIC");

        botTester.callback("topicskip");
        assertThat(botTester.chatState()).isEqualTo("AWAITING_EXERCISE_WORD_SELECTION");

        botTester.callback("exword:" + wordInfoId);
        botTester.callback("exdone");

        assertThat(botTester.chatState()).isEqualTo("STUDENT_OPTIONS");
        assertThat(botTester.documents()).hasSize(2);
        for (var document : botTester.documents()) {
            assertThat(document.fileName()).isNotBlank();
            assertThat(startsWithPdf(document.content())).isTrue();
        }

        var options = botTester.lastMessage();
        assertThat(options.keyboard()).isEqualTo(KeyboardKind.INLINE);
        assertThat(options.text()).contains("Иван");

        assertThat(botTester.messages()).anyMatch(message -> message.text() != null
                && message.text().contains("Генерирую"));
        assertThat(botTester.deletes()).isNotEmpty();

        assertThat(botTester.count("exercise")).isEqualTo(1);
        assertThat(botTester.rows("exercise").get(0).get("status")).isEqualTo("GENERATED");
        assertThat(botTester.count("exercise_word_info")).isEqualTo(1);

        var captor = ArgumentCaptor.forClass(GenerateExerciseCommand.class);
        verify(exerciseGenerator).generate(captor.capture(), any());
        var command = captor.getValue();
        assertThat(command.type()).isEqualTo(ExerciseType.FILL_IN_THE_BLANK);
        assertThat(command.topic()).isEqualTo(new CatalogTopic("present-simple"));
        assertThat(command.thematicTopic()).isNull();
        assertThat(command.cefrLevel()).isEqualTo("B1");
        assertThat(command.wordIds()).containsExactly(new WordInfoId(UUID.fromString(wordInfoId)));
    }

    private ExerciseDto exerciseDto() {
        return new ExerciseDto(
                ExerciseId.generate(),
                ExerciseType.FILL_IN_THE_BLANK,
                "Present Simple",
                null,
                "1. I ___ an apple.",
                List.of("eat"),
                List.of(new ExerciseItem("I ___ an apple.", List.of(), "eat")),
                ExerciseStatus.GENERATED
        );
    }

    private static boolean startsWithPdf(byte[] content) {
        return content.length >= 4
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F';
    }
}
