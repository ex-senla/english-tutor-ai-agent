/**
 * Generator bounded context.
 * <p>
 * Renders generated exercises into PDF documents (student exercise and teacher answers) via Thymeleaf templates.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"exercise :: exercise",
                "dictionary :: word",
                "shared :: shared"}
)
package com.hydroyura.eta.generator;
