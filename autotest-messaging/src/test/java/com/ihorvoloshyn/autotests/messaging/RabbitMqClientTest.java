package com.ihorvoloshyn.autotests.messaging;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMqClientTest {

    @Test
    void usesDefaultAmqpPort5672() throws Exception {
        RabbitMqClient client = new RabbitMqClient("10.20.30.40", "user", "password", "/");
        try {
            ConnectionFactory factory = connectionFactory(client);
            assertEquals(5672, factory.getPort());
        } finally {
            client.close();
        }
    }

    @Test
    void usesDefaultAmqpsPort5671AndTls() throws Exception {
        RabbitMqClient client = new RabbitMqClient("amqps://10.20.30.40", "user", "password", "/");
        try {
            ConnectionFactory factory = connectionFactory(client);
            assertEquals(5671, factory.getPort());
            assertTrue(factory.isSSL());
        } finally {
            client.close();
        }
    }

    @Test
    void preservesExplicitAmqpPort() throws Exception {
        RabbitMqClient client = new RabbitMqClient("amqp://10.20.30.40:15672", "user", "password", "/");
        try {
            assertEquals(15672, connectionFactory(client).getPort());
        } finally {
            client.close();
        }
    }

    @Test
    void preservesExplicitAmqpsPort() throws Exception {
        RabbitMqClient client = new RabbitMqClient("amqps://10.20.30.40:15671", "user", "password", "/");
        try {
            ConnectionFactory factory = connectionFactory(client);
            assertEquals(15671, factory.getPort());
            assertTrue(factory.isSSL());
        } finally {
            client.close();
        }
    }

    @Test
    void acceptsHostWithoutSchemeAndUsesAmqpDefault() throws Exception {
        RabbitMqClient client = new RabbitMqClient("rabbit.example.com", null, null, null);
        try {
            assertEquals(5672, connectionFactory(client).getPort());
            assertFalse(connectionFactory(client).isSSL());
        } finally {
            client.close();
        }
    }

    @Test
    void rejectsNullEndpoint() {
        assertThrows(IllegalArgumentException.class,
                () -> new RabbitMqClient(null, "user", "password", "/"));
    }

    private static ConnectionFactory connectionFactory(RabbitMqClient client) throws Exception {
        Field field = RabbitMqClient.class.getDeclaredField("factory");
        field.setAccessible(true);
        return (ConnectionFactory) field.get(client);
    }
}
