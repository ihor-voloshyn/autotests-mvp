package com.ihorvoloshyn.autotests.vault;

public final class AuthenticatedVaultClient {

    private final VaultAuthenticator authenticator;
    private final String baseUrl;

    public AuthenticatedVaultClient(String baseUrl, VaultAuthenticator authenticator) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        if (authenticator == null) {
            throw new IllegalArgumentException("authenticator must not be null");
        }
        this.baseUrl = baseUrl;
        this.authenticator = authenticator;
    }

    public VaultClient authenticate() {
        return new VaultClient(baseUrl, authenticator.authenticate());
    }
}
