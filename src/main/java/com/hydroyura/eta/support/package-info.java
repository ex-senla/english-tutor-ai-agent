/**
 * Support bounded context.
 * <p>
 * Manages teacher support threads and messages.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared :: shared",
                "teacher :: teacher"}
)
package com.hydroyura.eta.support;
