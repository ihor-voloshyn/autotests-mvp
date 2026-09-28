package com.ihorvoloshyn.autotests.db;

/**
 * Minimal database operations required by connection health checks.
 */
public interface DatabaseHealthClient {
    boolean isValid(int timeoutSeconds);
    boolean isSchemaAccessible();
}
