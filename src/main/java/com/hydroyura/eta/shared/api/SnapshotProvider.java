package com.hydroyura.eta.shared.api;

import java.util.Map;

/**
 * Exposes internal repository state for debugging.
 * Implemented by in-memory repositories.
 *
 * TODO: remove when switching to JPA/PostgreSQL
 */
public interface SnapshotProvider {

    Map<?, ?> snapshot();
}
