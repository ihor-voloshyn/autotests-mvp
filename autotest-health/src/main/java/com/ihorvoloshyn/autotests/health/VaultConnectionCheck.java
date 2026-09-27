package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.vault.VaultClient;

public final class VaultConnectionCheck implements ConnectionCheck {
    private final VaultClient client;
    private final String path;

    public VaultConnectionCheck(VaultClient client, String path) {
        if (client == null) throw new IllegalArgumentException("client must not be null");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        this.client = client;
        this.path = path;
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        client.read(path);
        return ConnectionCheckResult.success("Vault", "Vault API is reachable and token is accepted", elapsed(start));
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
