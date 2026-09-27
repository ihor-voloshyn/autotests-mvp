package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;

public final class CamundaConnectionCheck implements ConnectionCheck {
    private final CamundaClient client;

    public CamundaConnectionCheck(CamundaClient client) {
        if (client == null) throw new IllegalArgumentException("client must not be null");
        this.client = client;
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        var response = client.get("/engine-rest/engine");
        int status = response.statusCode();
        return status < 500
                ? ConnectionCheckResult.success("Camunda", "Camunda REST responded with " + status, elapsed(start))
                : ConnectionCheckResult.failure("Camunda", "Camunda REST responded with " + status, elapsed(start));
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}