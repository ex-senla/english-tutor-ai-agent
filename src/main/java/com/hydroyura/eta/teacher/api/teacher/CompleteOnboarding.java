package com.hydroyura.eta.teacher.api.teacher;

/**
 * Отмечает онбординг преподавателя завершённым. Вызывается chatbot-ом при завершении
 * регистрации через разрешённую зависимость {@code teacher :: teacher}.
 */
public interface CompleteOnboarding {

    void execute(TeacherId teacherId);
}
