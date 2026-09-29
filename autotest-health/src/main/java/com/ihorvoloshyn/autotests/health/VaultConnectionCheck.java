package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.vault.VaultClient;\nimport com.ihorvoloshyn.autotests.vault.VaultKvVersion;

import java.util.Objects;
import java.util.function.Supplier;

public final class VaultConnectionCheck implements ConnectionCheck {
    private final Supplier<VaultClient> clientSupplier;
    private final String path;\n    private final String mount;\n    private final VaultKvVersion kvVersion;

    public VaultConnectionCheck(VaultClient client, String path) {
        this(() -> client, path);
    }

    public VaultConnectionCheck(Supplier<VaultClient> clientSupplier, String path) {
        this.clientSupplier = Objects.requireNonNull(clientSupplier, "clientSupplier must not be null");
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        this.path = path;\n        this.mount = null;\n        this.kvVersion = null;
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            VaultClient client = Objects.requireNonNull(
                    clientSupplier.get(),
                    "clientSupplier returned null");
            if (mount == null) {\n                client.read(path);\n            } else {\n                client.readData(mount, path, kvVersion);\n            }
            return ConnectionCheckResult.success(
                    "Vault",
                    "Vault API is reachable and token is accepted",
                    elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure(
                    "Vault",
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    elapsed(start));
        }
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
