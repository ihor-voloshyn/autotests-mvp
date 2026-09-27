package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.messaging.RabbitMqClient;

public final class RabbitMqConnectionCheck implements ConnectionCheck {
    private final RabbitMqClient client;

    public RabbitMqConnectionCheck(RabbitMqClient client) {
        if (client == null) throw new IllegalArgumentException("client must not be null");
        this.client = client;
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            client.connect();
            return ConnectionCheckResult.success("RabbitMQ", "AMQP connection is established", elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure("RabbitMQ", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed(start));
        }
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
