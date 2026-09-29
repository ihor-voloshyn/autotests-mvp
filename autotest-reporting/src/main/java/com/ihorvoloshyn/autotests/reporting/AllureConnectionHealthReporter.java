package com.ihorvoloshyn.autotests.reporting;

import com.ihorvoloshyn.autotests.health.ConnectionCheckResult;

import java.util.List;

/**
 * Publishes connection health results as Allure steps.
 */
public final class AllureConnectionHealthReporter {

    public void report(List<ConnectionCheckResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("results must not be null");
        }

        for (ConnectionCheckResult result : results) {
            if (result == null) {
                throw new IllegalArgumentException("results must not contain null");
            }

            String stepName = result.name() + " [" + (result.success() ? "PASS" : "FAIL") + "]";
            AllureSupport.step(stepName, () -> {
                AllureSupport.parameter("success", Boolean.toString(result.success()));
                AllureSupport.parameter("durationMs", Long.toString(result.durationMs()));
                AllureSupport.attachText("message", result.message() == null ? "" : result.message());
            });
        }
    }
}
