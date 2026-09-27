package com.ihorvoloshyn.autotests.core.config;

import java.util.Objects;
import java.util.function.Supplier;

public final class LazySecretResolver implements SecretResolver {
    private final Supplier<SecretResolver> supplier;
    private volatile SecretResolver delegate;

    public LazySecretResolver(Supplier<SecretResolver> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    @Override
    public String resolve(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        SecretResolver current = delegate;
        if (current == null) {
            synchronized (this) {
                current = delegate;
                if (current == null) {
                    current = Objects.requireNonNull(
                            supplier.get(),
                            "SecretResolver supplier returned null");
                    delegate = current;
                }
            }
        }
        return current.resolve(key);
    }
}
