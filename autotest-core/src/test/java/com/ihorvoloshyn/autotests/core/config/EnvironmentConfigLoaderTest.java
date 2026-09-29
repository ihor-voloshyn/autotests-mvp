package com.ihorvoloshyn.autotests.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentConfigLoaderTest {
    @Test
    void missingResourceUsesDefaults() {
        FrameworkConfig config = new EnvironmentConfigLoader("missing-test.properties").load();
        assertEquals(Environment.TEST, config.environment());
        assertEquals("http://localhost", config.baseUrl());
    }

    @Test
    void loadsApplicationProperties() {
        FrameworkConfig config = new EnvironmentConfigLoader("application.properties").load();
        assertTrue(config.hasProperty("test.environment"));
        assertTrue(config.hasProperty("service.base-url"));
        assertTrue(config.hasProperty("vault.url"));
    }

    @Test
    void blankResourceNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentConfigLoader(" "));
    }
}
