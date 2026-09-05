package com.hydroyura.eta.teacher.api.teacher;

public record RegisterTeacherCommand(
        Long telegramChatId,
        String name
) {
}
