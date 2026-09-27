package com.ihorvoloshyn.autotests.vault;

import com.ihorvoloshyn.autotests.core.config.SecretResolver;

public final class VaultSecretResolver implements SecretResolver {
    private final VaultSecretProvider provider;
    private final String path;

    public VaultSecretResolver(VaultSecretProvider provider, String path) {
        if (provider == null) throw new IllegalArgumentException("provider must not be null");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        this.provider = provider;
        this.path = path;
    }

    @Override
    public String resolve(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        return provider.getSecret(path, key);
    }
}
