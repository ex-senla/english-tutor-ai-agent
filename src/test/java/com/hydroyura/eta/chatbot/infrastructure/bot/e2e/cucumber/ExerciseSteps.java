package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import com.hydroyura.eta.chatbot.infrastructure.bot.e2e.KeyboardKind;
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
import com.hydroyura.eta.exercise.application.port.ExerciseGenerator;
import com.hydroyura.eta.exercise.domain.exercise.ExerciseStatus;
import com.hydroyura.eta.student.api.student.StudentId;
import com.hydroyura.eta.student.api.student.StudentQuery;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Шаги сценария генерации упражнения (AC-5), трассируемого на
 * {@code e2e-bot-testing / E2E-сценарий генерации упражнения / Полный флоу генерации и Индикатор
 * генерации}.
 */
public class ExerciseSteps {

    @Autowired
    private CucumberScenarioState state;

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private AddWordToDictionary addWordToDictionary;

    @Autowired
    private ExerciseGenerator exerciseGenerator;

    @И("в словарь добавлено слово {string} с переводом {string}")
    public void вСловарьДобавленоСлово(String word, String translation) {
        var dictionaryId = studentQuery.getDictionaryId(new StudentId(UUID.fromString(state.getStudentId())))
                .orElseThrow();
        var wordInfoId = addWordToDictionary.execute(
                new AddWordCommand(dictionaryId, word, Set.of(translation), PartOfSpeech.NOUN))
                .value()
                .toString();
        state.setWordInfoId(wordInfoId);
    }

    @И("мок генератора возвращает сгенерированное упражнение")
    public void мокГенератораВозвращаетУпражнение() {
        when(exerciseGenerator.generate(any(GenerateExerciseCommand.class), any()))
                .thenReturn(exerciseDto());
    }

    @Когда("учитель выбирает слово {string}")
    public void учительВыбираетСлово(String word) {
        state.getBotTester().callback("exword:" + state.getWordInfoId());
    }

    @Тогда("бот генерирует упражнение и отправляет два PDF")
    public void ботГенерируетУпражнение() {
        var botTester = state.getBotTester();
        assertThat(botTester.chatState()).isEqualTo("STUDENT_OPTIONS");
        assertThat(botTester.documents()).hasSize(2);
        for (var document : botTester.documents()) {
            assertThat(document.fileName()).isNotBlank();
            assertThat(startsWithPdf(document.content())).isTrue();
        }

        var options = botTester.lastMessage();
        assertThat(options.keyboard()).isEqualTo(KeyboardKind.INLINE);
        assertThat(options.text()).contains(state.getStudentName());

        assertThat(botTester.messages()).anyMatch(message -> message.text() != null
                && message.text().contains("Генерирую"));
        assertThat(botTester.deletes()).isNotEmpty();
    }

    @И("мок генератора вызван с типом {string}, темой {string} и выбранным словом")
    public void мокГенератораВызван(String type, String topic) {
        var captor = ArgumentCaptor.forClass(GenerateExerciseCommand.class);
        verify(exerciseGenerator).generate(captor.capture(), any());
        var command = captor.getValue();
        assertThat(command.type()).isEqualTo(ExerciseType.valueOf(type));
        assertThat(command.topic()).isEqualTo(new CatalogTopic(topic));
        assertThat(command.thematicTopic()).isNull();
        assertThat(command.cefrLevel()).isEqualTo(state.getStudentLevel());
        assertThat(command.wordIds()).containsExactly(new WordInfoId(UUID.fromString(state.getWordInfoId())));
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
                ExerciseStatus.GENERATED);
    }

    private static boolean startsWithPdf(byte[] content) {
        return content.length >= 4
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F';
    }
}
