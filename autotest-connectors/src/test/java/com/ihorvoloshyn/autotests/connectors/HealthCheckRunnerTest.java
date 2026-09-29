package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckRunnerTest {

    @Test
    void runsEmptyConfiguredHealthSetWithoutExternalCalls() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST, "", "", "", Map.of());

        assertTrue(new HealthCheckRunner(new HealthCheckFactory(config)).run().isEmpty());
    }

    @Test
    void rejectsNullFactory() {
        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckRunner(null));
    }
}
