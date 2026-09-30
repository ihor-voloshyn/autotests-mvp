package com.ihorvoloshyn.autotests.health;

public record HealthRunOptions(boolean parallel, boolean failFast) {
    public static HealthRunOptions sequential() {
        return new HealthRunOptions(false, false);
    }

    public static HealthRunOptions parallelExecution() {
        return new HealthRunOptions(true, false);
    }

    public static HealthRunOptions parallelFailFast() {
        return new HealthRunOptions(true, true);
    }
}
