package com.ihorvoloshyn.autotests.db;

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
        if (port != null && (port < 1 || port > 65535)) throw new IllegalArgumentException("port must be between 1 and 65535");
        if (database == null || database.isBlank()) throw new IllegalArgumentException("database/service name must not be blank");
        if (schema == null || schema.isBlank()) throw new IllegalArgumentException("database schema is required");
        username = username == null ? "" : username;
        password = password == null ? "" : password;
    }

    public int effectivePort() { return port == null ? (type == DatabaseType.POSTGRESQL ? 5432 : 1521) : port; }
}