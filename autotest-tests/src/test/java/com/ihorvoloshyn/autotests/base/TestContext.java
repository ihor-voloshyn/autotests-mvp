package com.ihorvoloshyn.autotests.base;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.rest.RestClient;
import com.ihorvoloshyn.autotests.vault.AuthenticatedVaultClient;
import com.ihorvoloshyn.autotests.vault.BasicAuthVaultAuthenticator;
import com.ihorvoloshyn.autotests.vault.VaultClient;

public final class TestContext {

    private final FrameworkConfig config;
    private final RestClient rest;
    private final CamundaClient camunda;
    private final VaultClient vault;

    public TestContext(FrameworkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        this.config = config;
        this.rest = new RestClient(config.baseUrl());
        this.camunda = new CamundaClient(
                config.property("camunda.base-url", config.baseUrl()));

        String username = config.property("vault.username", "");
        String password = config.property("vault.password", "");
        String authPath = config.property("vault.auth.path", "");

        if (username.isBlank() || authPath.isBlank()) {
            this.vault = null;
        } else {
            BasicAuthVaultAuthenticator authenticator =
                    new BasicAuthVaultAuthenticator(
                            config.vaultUrl(), authPath, username, password);
            this.vault = new AuthenticatedVaultClient(
                    config.vaultUrl(), authenticator).authenticate();
        }
    }

    public FrameworkConfig config() {
        return config;
    }

    public RestClient rest() {
        return rest;
    }

    public CamundaClient camunda() {
        return camunda;
    }

    public VaultClient vault() {
        if (vault == null) {
            throw new IllegalStateException(
                    "Vault is not configured. Set vault.auth.path and Vault credentials.");
        }
        return vault;
    }

    public boolean hasVault() {
        return vault != null;
    }
}
