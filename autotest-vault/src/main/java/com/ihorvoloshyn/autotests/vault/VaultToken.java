package com.ihorvoloshyn.autotests.vault;

public record VaultToken(String value) {

    public VaultToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vault token must not be blank");
        }
    }
}
