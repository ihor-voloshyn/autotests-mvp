package com.ihorvoloshyn.autotests.vault;

public interface VaultSecretProvider {

    String getSecret(String path, String key);

    default String requireSecret(String path, String key) {
        String value = getSecret(path, key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required Vault secret is missing: " + path + "/" + key);
        }
        return value;
    }
}