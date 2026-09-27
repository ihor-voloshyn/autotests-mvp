package com.ihorvoloshyn.autotests.core.config;

import java.util.List;

public final class CompositeSecretResolver implements SecretResolver {
    private final List<SecretResolver> resolvers;

    public CompositeSecretResolver(List<SecretResolver> resolvers) {
        if (resolvers == null) throw new IllegalArgumentException("resolvers must not be null");
        this.resolvers = List.copyOf(resolvers);
    }

    @Override
    public String resolve(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        for (SecretResolver resolver : resolvers) {
            if (resolver == null) continue;
            String value = resolver.resolve(key);
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
