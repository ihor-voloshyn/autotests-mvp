package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.health.ConnectionCheckResult;
import com.ihorvoloshyn.autotests.health.HealthRunOptions;
import com.ihorvoloshyn.autotests.health.HealthRunResult;

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
        return run(HealthRunOptions.sequential()).results();
    }

    public HealthRunResult run(HealthRunOptions options) {
        if (options == null) throw new IllegalArgumentException("options must not be null");
        return factory.createService().run(options);
    }
}
