package com.ihorvoloshyn.autotests.health;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
        return run(HealthRunOptions.sequential()).results();
    }

    public HealthRunResult run() {
        return run(HealthRunOptions.sequential());
    }

    public HealthRunResult run(HealthRunOptions options) {
        if (options == null) throw new IllegalArgumentException("options must not be null");
        long start = System.nanoTime();

        List<ConnectionCheckResult> results = options.parallel()
                ? runParallel(options.failFast())
                : runSequential(options.failFast());

        return new HealthRunResult(results, elapsed(start));
    }

    private List<ConnectionCheckResult> runSequential(boolean failFast) {
        java.util.ArrayList<ConnectionCheckResult> results = new java.util.ArrayList<>();
        for (ConnectionCheck check : checks) {
            ConnectionCheckResult result = safeCheck(check);
            results.add(result);
            if (failFast && !result.success()) break;
        }
        return List.copyOf(results);
    }

    private List<ConnectionCheckResult> runParallel(boolean failFast) {
        if (checks.isEmpty()) return List.of();

        List<CompletableFuture<ConnectionCheckResult>> futures = checks.stream()
                .map(check -> CompletableFuture.supplyAsync(() -> safeCheck(check)))
                .toList();

        java.util.ArrayList<ConnectionCheckResult> results = new java.util.ArrayList<>();
        for (CompletableFuture<ConnectionCheckResult> future : futures) {
            try {
                ConnectionCheckResult result = future.join();
                results.add(result);
                if (failFast && !result.success()) break;
            } catch (CompletionException e) {
                Throwable cause = e.getCause() == null ? e : e.getCause();
                results.add(ConnectionCheckResult.failure(
                        "HealthCheck",
                        cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                        0));
                if (failFast) break;
            }
        }
        return List.copyOf(results);
    }

    private ConnectionCheckResult safeCheck(ConnectionCheck check) {
        long start = System.nanoTime();
        try {
            ConnectionCheckResult result = check.check();
            if (result == null) {
                return ConnectionCheckResult.failure(
                        check.getClass().getSimpleName(), "Check returned null", elapsed(start));
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
