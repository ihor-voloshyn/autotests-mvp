package com.ihorvoloshyn.autotests.vault;

import java.util.Map;

public final class VaultSecretStore implements VaultSecretProvider {

    private final VaultClient client;
    private final String mount;
    private final VaultKvVersion version;

    public VaultSecretStore(VaultClient client, String mount, VaultKvVersion version) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        if (mount == null || mount.isBlank()) {
            throw new IllegalArgumentException("mount must not be blank");
        }
        this.client = client;
        this.mount = trimSlashes(mount);
        this.version = version == null ? VaultKvVersion.KV2 : version;
    }

    @Override
    public String getSecret(String path, String key) {
        Map<String, Object> data = client.readData(mount, path, version);
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public Map<String, Object> getSecrets(String path) {
        return client.readData(mount, path, version);
    }

    private static String trimSlashes(String value) {
        return value.replaceAll("^/+|/+$", "");
    }
}
