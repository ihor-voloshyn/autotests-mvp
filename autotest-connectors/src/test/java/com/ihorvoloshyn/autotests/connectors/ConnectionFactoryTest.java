package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.core.config.SecretResolver;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionFactoryTest {

    @Test
    void createsRestClientFromConfiguration() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST, "", "", "",
                Map.of("service.url", "https://example.com:8443/api"));

        var factory = new ConnectionFactory(config);

        assertNotNull(factory.rest("service"));
    }

    @Test
    void createsRestClientUsingSecretResolver() {
        FrameworkConfig config = new FrameworkConfig(
                Environment.TEST, "", "", "",
                Map.of(
                        "service.url", "https://example.com:8443/api",
                        "service.username-key", "service-user",
                        "service.password-key", "service-password"));

        var secrets = SecretResolver.fromMap(Map.of(
                "service-user", "user",
                "service-password", "password"));

        var factory = new ConnectionFactory(config);

        assertNotNull(factory.rest("service", secrets));
    }

    @Test
    void rejectsNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new ConnectionFactory(null));
    }
}
