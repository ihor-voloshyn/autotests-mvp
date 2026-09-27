package com.ihorvoloshyn.autotests.health;

public record ConnectionCheckResult(String name, boolean success, String message, long durationMs) {
    public static ConnectionCheckResult success(String name, String message, long durationMs) {
        return new ConnectionCheckResult(name, true, message, durationMs);
    }
    public static ConnectionCheckResult failure(String name, String message, long durationMs) {
        return new ConnectionCheckResult(name, false, message, durationMs);
    }
}