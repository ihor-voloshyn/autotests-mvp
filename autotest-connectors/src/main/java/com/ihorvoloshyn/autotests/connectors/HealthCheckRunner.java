package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.health.ConnectionCheckResult;

import java.util.List;

/**
 * Executes the configured health checks and returns normalized results.
 */
public final class HealthCheckRunner {
    private final HealthCheckFactory factory;

    public HealthCheckRunner(HealthCheckFactory factory) {
        if (factory == null) throw new IllegalArgumentException("factory must not be null");
        this.factory = factory;
    }

    public List<ConnectionCheckResult> run() {
        return factory.createService().checkAll();
    }
}
