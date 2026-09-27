package com.ihorvoloshyn.autotests.core.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServiceConfigTest {
    @Test
    void createsServiceConfigFromCommonProperties() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST, "", "", "",
                Map.of("service.url", "https://example.com:8443/api",
                       "service.username", "user",
                       "service.password", "secret"));

        ServiceConfig service = ServiceConfig.from(config, "service", "service");

        assertEquals("service", service.name());
        assertEquals("example.com", service.endpoint().host());
        assertEquals(8443, service.endpoint().port());
        assertEquals("/api", service.endpoint().path());
        assertTrue(service.hasCredentials());
    }

    @Test
    void requiresEndpoint() {
        FrameworkConfig config = FrameworkConfig.defaults();
        assertThrows(IllegalStateException.class,
                () -> ServiceConfig.from(config, "missing", "Missing"));
    }
}
