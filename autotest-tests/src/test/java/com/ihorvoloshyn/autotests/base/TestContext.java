package com.ihorvoloshyn.autotests.base;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.ihorvoloshyn.autotests.core.config.*;
import com.ihorvoloshyn.autotests.rest.RestClient;
import com.ihorvoloshyn.autotests.vault.*;

public final class TestContext {
    private final FrameworkConfig config;
    private final RestClient rest;
    private final CamundaClient camunda;
    private final VaultClient vault;

    public TestContext(FrameworkConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
        this.rest = new RestClient(config.baseUrl());
        this.camunda = new CamundaClient(config.property("camunda.base-url", config.baseUrl()));
        this.vault = createVault(config);
    }

    private static VaultClient createVault(FrameworkConfig config) {
        String username = config.property("vault.username", "");
        String password = config.property("vault.password", "");
        String authPath = config.property("vault.auth.path", "");
        if (username.isBlank() || authPath.isBlank()) return null;

        BasicAuthVaultAuthenticator authenticator =
                new BasicAuthVaultAuthenticator(config.vaultUrl(), authPath, username, password);
        return new AuthenticatedVaultClient(config.vaultUrl(), authenticator).authenticate();
    }

    public FrameworkConfig config() { return config; }
    public RestClient rest() { return rest; }
    public CamundaClient camunda() { return camunda; }

    public VaultClient vault() {
        if (vault == null) {
            throw new IllegalStateException(
                    "Vault is not configured. Set vault.auth.path and Vault credentials.");
        }
        return vault;
    }

    public boolean hasVault() { return vault != null; }

    public SecretResolver configSecrets() {
        return new ConfigurationSecretResolver(config);
    }

    public SecretResolver vaultSecrets(String mount, String path, VaultKvVersion version) {
        if (!hasVault()) throw new IllegalStateException("Vault is not configured");
        return new VaultSecretResolver(new VaultSecretStore(vault, mount, version), path);
    }

    public SecretResolver secrets(String mount, String path, VaultKvVersion version) {
        SecretResolver configResolver = configSecrets();
        if (!hasVault()) return configResolver;
        return new CompositeSecretResolver(java.util.List.of(
                configResolver,
                vaultSecrets(mount, path, version)));
    }
}
