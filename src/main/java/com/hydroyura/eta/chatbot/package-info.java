/**
 * Chatbot state machine domain.
 * Platform-agnostic.
 */
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"teacher :: teacher", "student :: student",
        "student :: lesson", "dictionary :: dictionary", "dictionary :: word", "shared :: shared"}
)
package com.hydroyura.eta.chatbot;
