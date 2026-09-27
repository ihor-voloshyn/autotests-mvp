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
        properties.forEach((key, value) -> values.put(key.toString(), resolve(value.toString())));

        // Environment variables override application.properties.
        System.getenv().forEach((key, value) -> values.put(toPropertyKey(key), value));

        return new FrameworkConfig(
                Environment.from(value(values, "test.environment", "TEST")),
                value(values, "service.base-url", "http://localhost"),
                value(values, "soap.base-url", "http://localhost"),
                value(values, "vault.url", "http://localhost:8200"),
                values);
    }

    private static String resolve(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("${") && value.endsWith("}")) {
            String expression = value.substring(2, value.length() - 1);
            int separator = expression.indexOf(':');
            String variable = separator >= 0 ? expression.substring(0, separator) : expression;
            String fallback = separator >= 0 ? expression.substring(separator + 1) : "";
            return System.getenv().getOrDefault(variable, fallback);
        }
        return value;
    }

    private static String toPropertyKey(String environmentName) {
        return environmentName.toLowerCase().replace('_', '.');
    }

    private static String value(Map<String, String> values, String key, String fallback) {
        return values.getOrDefault(key, fallback);
    }
}
