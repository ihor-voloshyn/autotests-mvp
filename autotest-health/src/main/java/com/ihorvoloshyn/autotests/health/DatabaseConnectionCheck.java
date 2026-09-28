package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.db.DatabaseHealthClient;
import com.ihorvoloshyn.autotests.db.JdbcClient;

import java.util.Objects;
import java.util.function.Supplier;

public final class DatabaseConnectionCheck implements ConnectionCheck {
    private final String name;
    private final Supplier<? extends DatabaseHealthClient> clientSupplier;
    private volatile DatabaseHealthClient client;

    public DatabaseConnectionCheck(String name, JdbcClient client) {
        this(name, () -> client);
    }

    public DatabaseConnectionCheck(String name, Supplier<? extends DatabaseHealthClient> clientSupplier) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.clientSupplier = Objects.requireNonNull(clientSupplier, "clientSupplier must not be null");
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            DatabaseHealthClient current = client;
            if (current == null) {
                synchronized (this) {
                    current = client;
                    if (current == null) {
                        current = Objects.requireNonNull(
                                clientSupplier.get(),
                                "clientSupplier returned null");
                        client = current;
                    }
                }
            }

            if (!current.isValid(5)) {
                return ConnectionCheckResult.failure(
                        name, "JDBC connection validation failed", elapsed(start));
            }
            if (!current.isSchemaAccessible()) {
                return ConnectionCheckResult.failure(
                        name, "Configured database schema is not accessible", elapsed(start));
            }
            return ConnectionCheckResult.success(
                    name,
                    "JDBC connection and configured schema are accessible",
                    elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure(
                    name,
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    elapsed(start));
        }
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
