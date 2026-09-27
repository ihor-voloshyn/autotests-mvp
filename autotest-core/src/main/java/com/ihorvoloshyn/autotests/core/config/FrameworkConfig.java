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

    public static FrameworkConfig defaults() {
        return new FrameworkConfig(
                Environment.TEST,
                "http://localhost",
                "http://localhost",
                "http://localhost:8200",
                Map.of());
    }
}
