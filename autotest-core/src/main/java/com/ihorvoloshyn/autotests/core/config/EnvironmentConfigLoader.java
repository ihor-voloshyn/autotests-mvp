package com.ihorvoloshyn.autotests.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class EnvironmentConfigLoader implements ConfigLoader {

    private final String resourceName;

    public EnvironmentConfigLoader() {
        this("application.properties");
    }

    public EnvironmentConfigLoader(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public FrameworkConfig load() {
        Properties properties = new Properties();

        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load configuration: " + resourceName, e);
        }

        Map<String, String> values = new HashMap<>();
        properties.forEach((key, value) -> values.put(key.toString(), value.toString()));

        return new FrameworkConfig(
                Environment.from(value(values, "test.environment", "TEST")),
                value(values, "service.base-url", "http://localhost"),
                value(values, "soap.base-url", "http://localhost"),
                value(values, "vault.url", "http://localhost:8200"),
                values);
    }

    private static String value(Map<String, String> values, String key, String fallback) {
        return values.getOrDefault(key, fallback);
    }
}
