package com.ihorvoloshyn.autotests.core.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FrameworkConfigTest {

    @Test
    void returnsConfiguredProperty() {
        var config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of("service.url", "http://example"));
        assertEquals("http://example", config.property("service.url", "fallback"));
        assertEquals("http://example", config.propertyOrEmpty("service.url"));
        assertTrue(config.hasProperty("service.url"));
    }

    @Test
    void returnsFallbackForMissingProperty() {
        var config = FrameworkConfig.defaults();
        assertEquals("fallback", config.property("missing", "fallback"));
        assertEquals("", config.propertyOrEmpty("missing"));
        assertFalse(config.hasProperty("missing"));
    }

    @Test
    void blankPropertyIsNotConsideredConfigured() {
        var config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of("service.url", " "));
        assertFalse(config.hasProperty("service.url"));
        assertEquals(" ", config.propertyOrEmpty("service.url"));
    }

    @Test
    void requiredPropertyRejectsMissingOrBlankValues() {
        var config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of("blank", " "));
        assertThrows(IllegalStateException.class, () -> config.requiredProperty("missing"));
        assertThrows(IllegalStateException.class, () -> config.requiredProperty("blank"));
    }

    @Test
    void rejectsBlankConfigurationKeys() {
        var config = FrameworkConfig.defaults();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> config.property(" ", "")),
                () -> assertThrows(IllegalArgumentException.class, () -> config.hasProperty(" ")),
                () -> assertThrows(IllegalArgumentException.class, () -> config.requiredProperty(" ")));
    }

    @Test
    void copiesPropertiesDefensively() {
        var config = new FrameworkConfig(Environment.TEST, "", "", "", Map.of("key", "value"));
        assertThrows(UnsupportedOperationException.class,
                () -> config.properties().put("other", "value"));
    }
}
