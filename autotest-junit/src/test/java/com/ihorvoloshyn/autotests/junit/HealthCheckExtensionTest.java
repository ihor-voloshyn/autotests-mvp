package com.ihorvoloshyn.autotests.junit;

import com.ihorvoloshyn.autotests.connectors.HealthCheckFactory;
import com.ihorvoloshyn.autotests.connectors.HealthCheckRunner;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.health.HealthRunOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HealthCheckExtensionTest {

    @Test
    void defaultConstructorCreatesUsableExtension() {
        assertNotNull(new HealthCheckExtension());
    }

    @Test
    void emptyConfigurationIsSuccessfulInReportOnlyMode() {
        HealthCheckRunner runner = new HealthCheckRunner(
                new HealthCheckFactory(FrameworkConfig.defaults()));

        HealthCheckExtension extension = new HealthCheckExtension(
                runner,
                HealthRunOptions.sequential(),
                HealthCheckExtension.FailurePolicy.REPORT_ONLY,
                new com.ihorvoloshyn.autotests.reporting.AllureConnectionHealthReporter());

        assertDoesNotThrow(() -> extension.beforeAll(null));
    }

    @Test
    void emptyConfigurationProducesEmptySuccessfulRun() {
        HealthCheckRunner runner = new HealthCheckRunner(
                new HealthCheckFactory(FrameworkConfig.defaults()));

        var result = runner.run(HealthRunOptions.parallelExecution());

        assertEquals(0, result.total());
        assertEquals(0, result.failed());
        assertEquals(0, result.successful());
        assertEquals(true, result.success());
    }
}
