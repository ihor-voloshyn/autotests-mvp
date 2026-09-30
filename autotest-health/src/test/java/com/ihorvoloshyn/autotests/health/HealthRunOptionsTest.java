package com.ihorvoloshyn.autotests.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthRunOptionsTest {

    @Test
    void sequentialDisablesParallelAndFailFast() {
        HealthRunOptions options = HealthRunOptions.sequential();

        assertFalse(options.parallel());
        assertFalse(options.failFast());
    }

    @Test
    void parallelExecutionEnablesOnlyParallel() {
        HealthRunOptions options = HealthRunOptions.parallelExecution();

        assertTrue(options.parallel());
        assertFalse(options.failFast());
    }

    @Test
    void parallelFailFastEnablesBothFlags() {
        HealthRunOptions options = HealthRunOptions.parallelFailFast();

        assertTrue(options.parallel());
        assertTrue(options.failFast());
    }
}
