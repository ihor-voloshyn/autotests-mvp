package com.ihorvoloshyn.autotests.core.config;

import java.util.Map;
import java.util.Objects;

public interface SecretResolver {
    String resolve(String key);

    default String required(String key) {
        String value = resolve(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required secret is missing: " + key);
        }
        return value;
    }

    static SecretResolver fromMap(Map<String, String> secrets) {
        Map<String, String> source = secrets == null ? Map.of() : Map.copyOf(secrets);
        return key -> source.get(key);
    }

    static SecretResolver empty() {
        return key -> null;
    }
}
