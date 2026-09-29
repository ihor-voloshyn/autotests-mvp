package com.ihorvoloshyn.autotests.health;

import java.util.List;

public final class ConnectionHealthService {
    private final List<ConnectionCheck> checks;

    public ConnectionHealthService(List<ConnectionCheck> checks) {
        if (checks == null) throw new IllegalArgumentException("checks must not be null");
        if (checks.stream().anyMatch(check -> check == null)) {
            throw new IllegalArgumentException("checks must not contain null");
        }
        this.checks = List.copyOf(checks);
    }

    public List<ConnectionCheckResult> checkAll() {
        return checks.stream().map(this::safeCheck).toList();
    }

    private ConnectionCheckResult safeCheck(ConnectionCheck check) {
        long start = System.nanoTime();
        try {
            ConnectionCheckResult result = check.check();
            if (result == null) {
                return ConnectionCheckResult.failure(check.getClass().getSimpleName(), "Check returned null", elapsed(start));
            }
            return result;
        } catch (Exception e) {
            return ConnectionCheckResult.failure(
                    check.getClass().getSimpleName(),
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    elapsed(start));
        }
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}