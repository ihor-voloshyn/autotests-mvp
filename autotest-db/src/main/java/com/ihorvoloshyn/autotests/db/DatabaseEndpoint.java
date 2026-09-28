package com.ihorvoloshyn.autotests.db;

import com.ihorvoloshyn.autotests.core.config.Credentials;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.core.config.SecretResolver;

public record DatabaseEndpoint(
        DatabaseType type,
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password) {

    public DatabaseEndpoint {
        if (type == null) throw new IllegalArgumentException("database type must not be null");
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host must not be blank");
        port = port == null ? defaultPort(type) : port;
        if (port < 1 || port > 65535) throw new IllegalArgumentException("port must be between 1 and 65535");
        if (database == null || database.isBlank()) throw new IllegalArgumentException("database/service name must not be blank");
        if (schema == null || schema.isBlank()) throw new IllegalArgumentException("database schema is required");
        username = username == null ? "" : username;
        password = password == null ? "" : password;
    }

    public static DatabaseEndpoint from(FrameworkConfig config, String prefix, DatabaseType type, SecretResolver secrets) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        if (prefix == null || prefix.isBlank()) throw new IllegalArgumentException("prefix must not be blank");
        if (type == null) throw new IllegalArgumentException("database type must not be null");
        if (secrets == null) throw new IllegalArgumentException("secrets must not be null");

        String usernameKey = configuredKey(config, prefix + ".username-key", prefix + ".username");
        String passwordKey = configuredKey(config, prefix + ".password-key", prefix + ".password");
        Credentials credentials = new Credentials(secrets.resolve(usernameKey), secrets.resolve(passwordKey));

        String portValue = config.property(prefix + ".port", "");
        Integer port = portValue.isBlank() ? null : Integer.valueOf(portValue);
        return new DatabaseEndpoint(
                type,
                config.requiredProperty(prefix + ".host"),
                port,
                config.requiredProperty(prefix + ".database"),
                config.requiredProperty(prefix + ".schema"),
                credentials.username(),
                credentials.password());
    }

    private static String configuredKey(FrameworkConfig config, String key, String fallback) {
        String value = config.property(key, "");
        return value.isBlank() ? fallback : value;
    }

    private static int defaultPort(DatabaseType type) {
        return type == DatabaseType.POSTGRESQL ? 5432 : 1521;
    }

    public int effectivePort() {
        return port;
    }
}
