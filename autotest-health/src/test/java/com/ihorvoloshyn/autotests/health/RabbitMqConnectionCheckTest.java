package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.messaging.RabbitMqClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMqConnectionCheckTest {

    @Test
    void rejectsNullClient() {
        assertThrows(IllegalArgumentException.class,
                () -> new RabbitMqConnectionCheck(null));
    }

    @Test
    void returnsFailureWhenRabbitMqIsNotReachable() {
        var client = new RabbitMqClient("127.0.0.1:1", null, null, "/");
        try {
            ConnectionCheckResult result = new RabbitMqConnectionCheck(client).check();

            assertFalse(result.success());
            assertEquals("RabbitMQ", result.name());
            assertTrue(result.message().contains("RabbitMQ"));
        } finally {
            client.close();
        }
    }
}
