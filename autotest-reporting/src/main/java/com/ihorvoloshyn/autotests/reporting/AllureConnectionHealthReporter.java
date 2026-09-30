package com.ihorvoloshyn.autotests.reporting;

import com.ihorvoloshyn.autotests.health.ConnectionCheckResult;
import com.ihorvoloshyn.autotests.health.HealthRunResult;

import java.util.List;

/**
 * Publishes connection health results as Allure steps.
 */
public final class AllureConnectionHealthReporter {

    public void report(List<ConnectionCheckResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("results must not be null");
        }
        report(new HealthRunResult(results, 0));
    }

    public void report(HealthRunResult run) {
        if (run == null) {
            throw new IllegalArgumentException("run must not be null");
        }

        AllureSupport.step(
                "Connection health: " + run.successful() + "/" + run.total() + " passed",
                () -> {
                    AllureSupport.parameter("total", Long.toString(run.total()));
                    AllureSupport.parameter("successful", Long.toString(run.successful()));
                    AllureSupport.parameter("failed", Long.toString(run.failed()));
                    AllureSupport.parameter("durationMs", Long.toString(run.durationMs()));
                });

        for (ConnectionCheckResult result : run.results()) {
            String stepName = result.name() + " [" + (result.success() ? "PASS" : "FAIL") + "]";
            AllureSupport.step(stepName, () -> {
                AllureSupport.parameter("success", Boolean.toString(result.success()));
                AllureSupport.parameter("durationMs", Long.toString(result.durationMs()));
                AllureSupport.attachText("message", result.message() == null ? "" : result.message());
            });
        }
    }
}
