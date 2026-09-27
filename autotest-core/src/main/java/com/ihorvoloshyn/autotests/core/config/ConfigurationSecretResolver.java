package com.ihorvoloshyn.autotests.core.config;

public final class ConfigurationSecretResolver implements SecretResolver {
    private final FrameworkConfig config;
    private final String prefix;

    public ConfigurationSecretResolver(FrameworkConfig config) {
        this(config, "");
    }

    public ConfigurationSecretResolver(FrameworkConfig config, String prefix) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public String resolve(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        return config.property(prefix + key, null);
    }
}
