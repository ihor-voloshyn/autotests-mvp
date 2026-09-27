package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.infrastructure.ElkClient;

public final class ElkConnectionCheck implements ConnectionCheck {
    private final ElkClient client;

    public ElkConnectionCheck(ElkClient client) {
        if (client == null) throw new IllegalArgumentException("client must not be null");
        this.client = client;
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            int status = client.healthStatus();
            return status / 100 == 2
                    ? ConnectionCheckResult.success("ELK", "Elasticsearch health endpoint responded with " + status, elapsed(start))
                    : ConnectionCheckResult.failure("ELK", "Elasticsearch health endpoint responded with " + status, elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure(
                    "ELK",
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    elapsed(start));
        }
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
