package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;

public final class CamundaConnectionCheck implements ConnectionCheck {
    private final CamundaClient client;
    public CamundaConnectionCheck(CamundaClient client) {
        if (client == null) throw new IllegalArgumentException("client must not be null");
        this.client = client;
    }
    @Override public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            int status = client.get("/engine-rest/engine").statusCode();
            return status >= 200 && status < 400
                    ? ConnectionCheckResult.success("Camunda", "Camunda REST responded with " + status, elapsed(start))
                    : ConnectionCheckResult.failure("Camunda", "Camunda REST responded with " + status, elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure("Camunda", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed(start));
        }
    }
    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
