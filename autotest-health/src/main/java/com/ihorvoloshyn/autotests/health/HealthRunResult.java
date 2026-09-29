package com.ihorvoloshyn.autotests.health;

import java.util.List;

public record HealthRunResult(List<ConnectionCheckResult> results, long durationMs) {
    public HealthRunResult {
        if (results == null) throw new IllegalArgumentException("results must not be null");
        if (results.stream().anyMatch(result -> result == null)) {
            throw new IllegalArgumentException("results must not contain null");
        }
        results = List.copyOf(results);
        if (durationMs < 0) throw new IllegalArgumentException("durationMs must not be negative");
    }

    public long total() {
        return results.size();
    }

    public long successful() {
        return results.stream().filter(ConnectionCheckResult::success).count();
    }

    public long failed() {
        return results.stream().filter(result -> !result.success()).count();
    }

    public boolean success() {
        return failed() == 0;
    }
}
