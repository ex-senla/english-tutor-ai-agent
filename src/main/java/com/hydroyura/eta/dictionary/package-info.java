/**
 * Dictionary bounded context.
 * <p>
 * Manages vocabulary words collected during lessons.
 * Supports word lookups, categorization, and dictionary export.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "shared :: shared"
)
package com.hydroyura.eta.dictionary;
