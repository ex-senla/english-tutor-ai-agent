/**
 * Debug module — exposes internal state for development and troubleshooting.
 *
 * TODO: remove entire module when switching to JPA/PostgreSQL (use Actuator or DB tools instead)
 */
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = "shared :: shared"
)
package com.hydroyura.eta.debug;
