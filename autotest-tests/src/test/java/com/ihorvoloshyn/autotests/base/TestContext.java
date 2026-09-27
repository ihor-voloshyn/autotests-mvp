package com.ihorvoloshyn.autotests.base;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.ihorvoloshyn.autotests.connectors.ConnectionFactory;
import com.ihorvoloshyn.autotests.core.config.*;
import com.ihorvoloshyn.autotests.rest.RestClient;
import com.ihorvoloshyn.autotests.vault.VaultClient;
import com.ihorvoloshyn.autotests.vault.VaultKvVersion;
import com.ihorvoloshyn.autotests.vault.VaultSecretProvider;
import com.ihorvoloshyn.autotests.vault.VaultSecretResolver;
import com.ihorvoloshyn.autotests.vault.VaultSecretStore;

import java.util.List;

public final class TestContext {
    private final FrameworkConfig config;
    private final ConnectionFactory connections;

    private volatile RestClient rest;
    private volatile CamundaClient camunda;
    private volatile VaultClient vault;

    public TestContext(FrameworkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
        this.connections = new ConnectionFactory(config);
    }

    public FrameworkConfig config() {
        return config;
    }

    public ConnectionFactory connections() {
        return connections;
    }

    public RestClient rest() {
        RestClient current = rest;
        if (current == null) {
            synchronized (this) {
                current = rest;
                if (current == null) {
                    current = connections.rest();
                    rest = current;
                }
            }
        }
        return current;
    }

    public CamundaClient camunda() {
        CamundaClient current = camunda;
        if (current == null) {
            synchronized (this) {
                current = camunda;
                if (current == null) {
                    current = connections.camunda();
                    camunda = current;
                }
            }
        }
        return current;
    }

    public VaultClient vault() {
        VaultClient current = vault;
        if (current == null) {
            synchronized (this) {
                current = vault;
                if (current == null) {
                    if (!isVaultConfigured()) {
                        throw new IllegalStateException(
                                "Vault is not configured. Set vault.url, vault.auth.path and Vault credentials.");
                    }
                    current = connections.vault("vault");
                    vault = current;
                }
            }
        }
        return current;
    }

    public boolean isVaultConfigured() {
        return !config.property("vault.url", "").isBlank()
                && !config.property("vault.auth.path", "").isBlank()
                && !config.property("vault.username", "").isBlank();
    }

    /**
     * Kept as a compatibility alias. It reports configuration state only and never connects to Vault.
     */
    public boolean hasVault() {
        return isVaultConfigured();
    }

    public SecretResolver configSecrets() {
        return new ConfigurationSecretResolver(config);
    }

    public SecretResolver vaultSecrets(String mount, String path, VaultKvVersion version) {
        if (!isVaultConfigured()) {
            throw new IllegalStateException("Vault is not configured");
        }
        VaultSecretProvider provider = new VaultSecretStore(vault(), mount, version);
        return new VaultSecretResolver(provider, path);
    }

    public SecretResolver secrets(String mount, String path, VaultKvVersion version) {
        SecretResolver configResolver = configSecrets();
        if (!isVaultConfigured()) {
            return configResolver;
        }
        return new CompositeSecretResolver(List.of(
                configResolver,
                vaultSecrets(mount, path, version)));
    }
}
