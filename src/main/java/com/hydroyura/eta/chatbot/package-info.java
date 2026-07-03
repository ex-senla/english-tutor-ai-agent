/**
 * Chatbot state machine domain.
 * <p>
 * Manages bot sessions, state transitions, and command execution.
 * Platform-agnostic — adapters (Telegram, etc.) depend on this module.
 */
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"teacher :: teacher", "student :: student",
        "student :: lesson", "dictionary :: dictionary", "dictionary :: word",
        "exercise :: exercise"}
)
package com.hydroyura.eta.chatbot;
