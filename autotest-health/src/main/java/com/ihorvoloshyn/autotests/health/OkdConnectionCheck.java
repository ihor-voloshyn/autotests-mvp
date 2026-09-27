package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.infrastructure.CommandExecutor;

public final class OkdConnectionCheck implements ConnectionCheck {
    private final CommandExecutor executor;
    private final String namespace;

    public OkdConnectionCheck(CommandExecutor executor, String namespace) {
        if (executor == null) throw new IllegalArgumentException("executor must not be null");
        if (namespace == null || namespace.isBlank()) throw new IllegalArgumentException("namespace must not be blank");
        this.executor = executor;
        this.namespace = namespace;
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            var result = executor.execute("oc", "get", "pods", "-n", namespace, "-o", "name");
            return result.success()
                    ? ConnectionCheckResult.success("OKD", "Namespace is accessible: " + namespace, elapsed(start))
                    : ConnectionCheckResult.failure("OKD", "oc get pods failed: " + result.output(), elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure("OKD", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed(start));
        }
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
