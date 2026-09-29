package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckFactoryTest {

    @Test
    void createsOnlyConfiguredChecks() {
        FrameworkConfig config = config(Map.of(
                "health.rest.url", "http://localhost:18080",
                "health.soap.url", "http://localhost:18081/service",
                "health.vault.url", "http://localhost:18200",
                "health.vault.username", "user",
                "health.vault.password", "password",
                "health.vault.check.path", "health",
                "health.vault.mount", "secret",
                "health.vault.kv-version", "KV2",
                "health.postgresql.host", "localhost",
                "health.postgresql.database", "testdb",
                "health.postgresql.schema", "public",
                "health.camunda.url", "http://localhost:18143",
                "health.rabbitmq.endpoint", "localhost:15672",
                "health.elk.url", "http://localhost:19200",
                "health.okd.namespace", "test"));

        List<String> names = new HealthCheckFactory(config).createChecks().stream()
                .map(check -> check.getClass().getSimpleName())
                .toList();

        assertEquals(9, names.size());
        assertTrue(names.containsAll(List.of(
                "HttpConnectionCheck",
                "SoapConnectionCheck",
                "VaultConnectionCheck",
                "DatabaseConnectionCheck",
                "CamundaConnectionCheck",
                "RabbitMqConnectionCheck",
                "ElkConnectionCheck",
                "OkdConnectionCheck")));
        assertEquals(2, names.stream().filter("DatabaseConnectionCheck"::equals).count());
    }

    @Test
    void createsNoChecksWhenHealthConfigurationIsEmpty() {
        HealthCheckFactory factory = new HealthCheckFactory(config(Map.of()));

        assertTrue(factory.createChecks().isEmpty());
        assertTrue(factory.createService().checkAll().isEmpty());
    }

    @Test
    void validatesPartialDatabaseConfiguration() {
        FrameworkConfig config = config(Map.of(
                "health.postgresql.host", "localhost",
                "health.postgresql.database", "testdb"));

        assertThrows(IllegalStateException.class,
                () -> new HealthCheckFactory(config).createChecks());
    }

    @Test
    void rejectsUnsupportedVaultKvVersion() {
        FrameworkConfig config = config(Map.of(
                "health.vault.url", "http://localhost:18200",
                "health.vault.username", "user",
                "health.vault.password", "password",
                "health.vault.check.path", "health",
                "health.vault.kv-version", "KV3"));

        assertThrows(IllegalArgumentException.class,
                () -> new HealthCheckFactory(config).createChecks());
    }

    private static FrameworkConfig config(Map<String, String> properties) {
        return new FrameworkConfig(Environment.TEST, "", "", "", properties);
    }
}
