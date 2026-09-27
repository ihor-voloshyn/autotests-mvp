package com.ihorvoloshyn.autotests.vault;

public record VaultProperties(
        String url,
        String authPath,
        String username,
        String password) {

    public VaultProperties {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Vault URL must not be blank");
        }
        if (authPath == null || authPath.isBlank()) {
            throw new IllegalArgumentException("Vault auth path must not be blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Vault username must not be blank");
        }
        password = password == null ? "" : password;
    }
}
