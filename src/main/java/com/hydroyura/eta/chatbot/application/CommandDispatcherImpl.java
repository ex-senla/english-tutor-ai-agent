package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.application.commands.*;
import com.hydroyura.eta.chatbot.domain.command.Command;
import com.hydroyura.eta.chatbot.domain.command.CommandDispatcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandDispatcherImpl implements CommandDispatcher {

    private final List<Command> templates;

    public CommandDispatcherImpl(CommandDispatcherConfig config) {
        this.templates = List.of(
            new StartCmd(config.findTeacher()),
            new RegisterCmd(config.registerTeacher(), config.findTeacher()),
            new NewStudentCmd(config.createStudentWithDictionary(), config.findTeacher()),
            new StartLessonCmd(config.startLesson(), config.findTeacher(), config.studentQuery()),
            new AddWordCmd(config.addWordToDictionary(), config.addWordToLesson(), config.studentQuery(), config.findTeacher()),
            new EndLessonCmd(config.endLesson()),
            new ExerciseCmd(config.findTeacher(), config.studentQuery(), config.generateExercise(), config.checkExercise()),
            new HelpCmd()
        );
    }

    @Override
    public Command dispatch(String text) {
        return templates.stream()
            .filter(c -> c.matches(text))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Command get(Class<? extends Command> clazz) {
        return templates.stream()
            .filter(c -> c.getClass().equals(clazz))
            .findFirst()
            .orElse(null);
    }

}
