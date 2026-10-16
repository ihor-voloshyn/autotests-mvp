package com.ihorvoloshyn.autotests.camunda;

import io.restassured.response.Response;

import java.time.Duration;
import java.util.function.Predicate;

public class CamundaProcessWaiter {

    private final CamundaProcessClient client;

    public CamundaProcessWaiter(CamundaProcessClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        this.client = client;
    }

    public Response waitForInstance(
            String instanceId,
            Predicate<Response> condition,
            Duration timeout,
            Duration pollInterval) {

        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("instanceId must not be blank");
        }
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (pollInterval == null || pollInterval.isZero() || pollInterval.isNegative()) {
            throw new IllegalArgumentException("pollInterval must be positive");
        }

        long timeoutNanos = timeout.toNanos();
        long pollNanos = pollInterval.toNanos();
        long start = System.nanoTime();
        Response last = null;

        while (System.nanoTime() - start < timeoutNanos) {
            last = client.getInstance(instanceId);
            if (condition.test(last)) {
                return last;
            }

            long elapsed = System.nanoTime() - start;
            long remaining = timeoutNanos - elapsed;
            if (remaining > 0) {
                sleepNanos(Math.min(pollNanos, remaining));
            }
        }

        throw new IllegalStateException(
                "Camunda condition was not satisfied within " + timeout +
                ". Last response status: " + (last == null ? "none" : last.statusCode()));
    }

    public Response waitUntilFinished(
            String instanceId,
            Duration timeout,
            Duration pollInterval) {

        return waitForInstance(
                instanceId,
                response -> response.statusCode() == 404 ||
                        "false".equalsIgnoreCase(response.jsonPath().getString("active")),
                timeout,
                pollInterval);
    }

    private static void sleepNanos(long nanos) {
        try {
            long millis = nanos / 1_000_000;
            int extraNanos = (int) (nanos % 1_000_000);
            Thread.sleep(millis, extraNanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Camunda wait interrupted", e);
        }
    }
}
