package com.ihorvoloshyn.autotests.health;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        ArrayList<ConnectionCheckResult> results = new ArrayList<>();
        for (ConnectionCheck check : checks) {
            ConnectionCheckResult result = safeCheck(check);
            results.add(result);
            if (failFast && !result.success()) break;
        }
        return List.copyOf(results);
    }

    private List<ConnectionCheckResult> runParallel(boolean failFast) {
        if (checks.isEmpty()) return List.of();

        ExecutorService executor = Executors.newFixedThreadPool(checks.size());
        try {
            return failFast
                    ? runParallelFailFast(executor)
                    : runParallelAll(executor);
        } finally {
            executor.shutdownNow();
        }
    }

    private List<ConnectionCheckResult> runParallelAll(ExecutorService executor) {
        List<Future<ConnectionCheckResult>> futures = checks.stream()
                .map(check -> executor.submit(() -> safeCheck(check)))
                .toList();

        ArrayList<ConnectionCheckResult> results = new ArrayList<>(futures.size());
        for (Future<ConnectionCheckResult> future : futures) {
            results.add(await(future));
        }
        return List.copyOf(results);
    }

    private List<ConnectionCheckResult> runParallelFailFast(ExecutorService executor) {
        CompletionService<ConnectionCheckResult> completionService =
                new ExecutorCompletionService<>(executor);
        List<Future<ConnectionCheckResult>> futures = new ArrayList<>(checks.size());

        for (ConnectionCheck check : checks) {
            futures.add(completionService.submit(() -> safeCheck(check)));
        }

        ArrayList<ConnectionCheckResult> results = new ArrayList<>();
        try {
            for (int i = 0; i < checks.size(); i++) {
                ConnectionCheckResult result = await(completionService.take());
                results.add(result);
                if (!result.success()) {
                    futures.forEach(future -> future.cancel(true));
                    break;
                }
            }
            return List.copyOf(results);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            futures.forEach(future -> future.cancel(true));
            return List.copyOf(results);
        }
    }

    private static ConnectionCheckResult await(Future<ConnectionCheckResult> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConnectionCheckResult.failure(
                    "HealthCheck", "Interrupted while waiting for check", 0);
        } catch (CancellationException e) {
            return ConnectionCheckResult.failure(
                    "HealthCheck", "Check was cancelled", 0);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            return ConnectionCheckResult.failure(
                    "HealthCheck",
                    cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                    0);
        }
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
