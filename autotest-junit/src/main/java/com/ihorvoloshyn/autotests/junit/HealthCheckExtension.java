package com.ihorvoloshyn.autotests.junit;

import com.ihorvoloshyn.autotests.connectors.HealthCheckRunner;
import com.ihorvoloshyn.autotests.health.HealthRunOptions;
import com.ihorvoloshyn.autotests.health.HealthRunResult;
import com.ihorvoloshyn.autotests.reporting.AllureConnectionHealthReporter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Objects;

/**
 * Runs configured infrastructure health checks before a JUnit 5 test class.
 */
public final class HealthCheckExtension implements BeforeAllCallback {

    public enum FailurePolicy {
        REPORT_ONLY,
        FAIL_ON_ANY_FAILURE
    }

    private final HealthCheckRunner runner;
    private final HealthRunOptions options;
    private final FailurePolicy failurePolicy;
    private final AllureConnectionHealthReporter reporter;

    public HealthCheckExtension(HealthCheckRunner runner) {
        this(runner, HealthRunOptions.sequential(), FailurePolicy.FAIL_ON_ANY_FAILURE, new AllureConnectionHealthReporter());
    }

    public HealthCheckExtension(
            HealthCheckRunner runner,
            HealthRunOptions options,
            FailurePolicy failurePolicy,
            AllureConnectionHealthReporter reporter) {
        this.runner = Objects.requireNonNull(runner, "runner must not be null");
        this.options = Objects.requireNonNull(options, "options must not be null");
        this.failurePolicy = Objects.requireNonNull(failurePolicy, "failurePolicy must not be null");
        this.reporter = Objects.requireNonNull(reporter, "reporter must not be null");
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        HealthRunResult result = runner.run(options);
        reporter.report(result);
        if (failurePolicy == FailurePolicy.FAIL_ON_ANY_FAILURE && !result.success()) {
            throw new IllegalStateException(
                    "Infrastructure health checks failed: "
                            + result.failed() + " of " + result.total());
        }
    }
}
