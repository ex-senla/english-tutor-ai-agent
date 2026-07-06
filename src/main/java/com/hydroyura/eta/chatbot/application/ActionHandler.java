package com.hydroyura.eta.chatbot.application;

import com.hydroyura.eta.chatbot.domain.action.Action;
import com.hydroyura.eta.chatbot.domain.action.ActionResult;
import com.hydroyura.eta.chatbot.domain.action.ActionResult.InlineButton;
import com.hydroyura.eta.chatbot.domain.statemachine.State;
import com.hydroyura.eta.chatbot.domain.statemachine.StateMachine;
import com.hydroyura.eta.teacher.api.teacher.FindTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacher;
import com.hydroyura.eta.teacher.api.teacher.RegisterTeacherCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionHandler {

    private final FindTeacher findTeacher;
    private final RegisterTeacher registerTeacher;

    public ActionResult handle(StateMachine sm, Action action) {
        return switch (sm.getState()) {
            case INITIAL -> handleInitial(sm, action);
            case ACTIVE -> handleActive(sm, action);
            case AWAITING_REGISTRATION_NAME -> handleAwaitingRegistrationName(sm, action);
            case IN_LESSON -> handleInLesson(sm, action);
            default -> new ActionResult.TextResponse("Unknown state: " + sm.getState());
        };
    }

    // ==================== INITIAL ====================

    private ActionResult handleInitial(StateMachine sm, Action action) {
        if (action instanceof Action.Command(var cmd, var userName)) {

            // /start — auto-register or welcome back
            if ("/start".equals(cmd)) {
                return findTeacher.findByTelegramChatId(sm.getId().chatId())
                        .map(teacherId -> {
                            log.info("Teacher already registered: chatId={}, teacherId={}", sm.getId().chatId(), teacherId);
                            sm.updateState(State.ACTIVE);
                            return welcomeBackMessage(userName);
                        })
                        .orElseGet(() -> {
                            log.info("Registering new teacher: chatId={}, userName={}", sm.getId().chatId(), userName);
                            registerTeacher.execute(new RegisterTeacherCommand(sm.getId().chatId(), userName));
                            sm.updateState(State.ACTIVE);
                            return welcomeMessage(userName);
                        });
            }
        }

        // Any other input in INITIAL — prompt to use /start
        return new ActionResult.TextResponse("👋 Please use /start to begin.");
    }

    // ==================== ACTIVE ====================

    private ActionResult handleActive(StateMachine sm, Action action) {
        if (action instanceof Action.Command(var cmd, var userName)) {
            return switch (cmd) {
                case "/start" -> welcomeBackMessage(userName);
                default -> new ActionResult.TextResponse("Unknown command: " + cmd);
            };
        }
        return new ActionResult.TextResponse("Use a command or the menu below.");
    }

    // ==================== AWAITING_REGISTRATION_NAME ====================

    private ActionResult handleAwaitingRegistrationName(StateMachine sm, Action action) {
        if (action instanceof Action.InputParam(var name)) {
            registerTeacher.execute(new RegisterTeacherCommand(sm.getId().chatId(), name));
            sm.updateState(State.ACTIVE);
            return welcomeMessage(name);
        }
        return new ActionResult.TextResponse("Please enter your name to register.");
    }

    // ==================== IN_LESSON ====================

    private ActionResult handleInLesson(StateMachine sm, Action action) {
        return new ActionResult.TextResponse("You are in a lesson. Use /end to finish.");
    }

    // ==================== helpers ====================

    private ActionResult welcomeMessage(String userName) {
        var text = "🎉 Welcome, " + userName + "! I'm your English tutor.\n\n" +
                "What would you like to do?";
        var keyboard = List.of(
                List.of(
                        new InlineButton("📚 My Students", "students:list"),
                        new InlineButton("📝 New Lesson", "lesson:new")
                ),
                List.of(
                        new InlineButton("📖 Dictionary", "dictionary:list"),
                        new InlineButton("🎯 Exercises", "exercise:menu")
                )
        );
        return new ActionResult.TextWithInlineKeyboard(text, keyboard);
    }

    private ActionResult welcomeBackMessage(String userName) {
        var text = "👋 Welcome back, " + userName + "!\n\nWhat would you like to do?";
        var keyboard = List.of(
                List.of(
                        new InlineButton("📚 My Students", "students:list"),
                        new InlineButton("📝 New Lesson", "lesson:new")
                ),
                List.of(
                        new InlineButton("📖 Dictionary", "dictionary:list"),
                        new InlineButton("🎯 Exercises", "exercise:menu")
                )
        );
        return new ActionResult.TextWithInlineKeyboard(text, keyboard);
    }
}
