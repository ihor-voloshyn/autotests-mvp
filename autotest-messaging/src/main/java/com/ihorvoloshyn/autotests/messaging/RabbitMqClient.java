package com.ihorvoloshyn.autotests.messaging;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public final class RabbitMqClient implements AutoCloseable {
    private final ConnectionFactory factory;
    private Connection connection;

    public RabbitMqClient(String host, int port, String username, String password, String virtualHost) {
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host must not be blank");
        if (port <= 0 || port > 65535) throw new IllegalArgumentException("port must be between 1 and 65535");
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        if (username != null && !username.isBlank()) factory.setUsername(username);
        if (password != null) factory.setPassword(password);
        if (virtualHost != null && !virtualHost.isBlank()) factory.setVirtualHost(virtualHost);
    }

    public synchronized Connection connect() {
        try {
            if (connection == null || !connection.isOpen()) connection = factory.newConnection();
            return connection;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot connect to RabbitMQ", e);
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    @Override
    public synchronized void close() {
        if (connection != null) {
            try { connection.close(); } catch (Exception ignored) { }
        }
    }
}