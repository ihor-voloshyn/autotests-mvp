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
        if (condition == null || timeout == null || pollInterval == null) {
            throw new IllegalArgumentException("condition, timeout and pollInterval are required");
        }

        long deadline = System.nanoTime() + timeout.toNanos();
        Response last = null;

        while (System.nanoTime() < deadline) {
            last = client.getInstance(instanceId);
            if (condition.test(last)) {
                return last;
            }
            sleep(pollInterval);
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

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Camunda wait interrupted", e);
        }
    }
}