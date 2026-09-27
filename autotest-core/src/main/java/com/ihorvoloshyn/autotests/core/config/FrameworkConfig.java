package com.ihorvoloshyn.autotests.core.config;

import java.util.Map;

public record FrameworkConfig(
        Environment environment,
        String baseUrl,
        String soapUrl,
        String vaultUrl,
        Map<String, String> properties) {

    public FrameworkConfig {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public String property(String key, String fallback) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Configuration key must not be blank");
        }
        return properties.getOrDefault(key, fallback);
    }

    public String requiredProperty(String key) {
        String value = property(key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required configuration property is missing: " + key);
        }
        return value;
    }

    public static FrameworkConfig defaults() {
        return new FrameworkConfig(
                Environment.TEST,
                "http://localhost",
                "http://localhost",
                "http://localhost:8200",
                Map.of());
    }
}
