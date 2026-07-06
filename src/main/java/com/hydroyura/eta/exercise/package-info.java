/**
 * Exercise bounded context.
 * <p>
 * Generates exercises based on specified topics using words from the student's dictionary.
 * Supports various exercise types (fill-in-the-blank, matching, translation, etc.).
 */
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"dictionary :: dictionary", "dictionary :: word", "shared :: shared"}
)
package com.hydroyura.eta.exercise;
