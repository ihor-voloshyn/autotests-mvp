package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.core.config.Environment;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.vault.VaultKvVersion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckFactoryTest {

    @Test
    void createsOnlyConfiguredChecks() {
        FrameworkConfig config = config(Map.ofEntries(
                Map.entry("health.rest.url", "http://localhost:18080"),
                Map.entry("health.soap.url", "http://localhost:18081/service"),
                Map.entry("health.vault.url", "http://localhost:18200"),
                Map.entry("health.vault.username", "user"),
                Map.entry("health.vault.password", "password"),
                Map.entry("health.vault.check.path", "health"),
                Map.entry("health.vault.mount", "secret"),
                Map.entry("health.vault.kv-version", "KV2"),
                Map.entry("health.postgresql.host", "localhost"),
                Map.entry("health.postgresql.database", "testdb"),
                Map.entry("health.postgresql.schema", "public"),
                Map.entry("health.oracle.host", "localhost"),
                Map.entry("health.oracle.database", "orcl"),
                Map.entry("health.oracle.schema", "APP"),
                Map.entry("health.camunda.url", "http://localhost:18143"),
                Map.entry("health.rabbitmq.endpoint", "localhost:15672"),
                Map.entry("health.elk.url", "http://localhost:19200"),
                Map.entry("health.okd.namespace", "test")));

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
    void buildsVaultKv2ApiPathFromMountAndSecretPath() {
        assertEquals("secret/data/health/check",
                HealthCheckFactory.vaultApiPath("/secret/", "/health/check", VaultKvVersion.KV2));
    }

    @Test
    void buildsVaultKv1ApiPathFromMountAndSecretPath() {
        assertEquals("secret/health/check",
                HealthCheckFactory.vaultApiPath("secret", "health/check", VaultKvVersion.KV1));
    }

    @Test
    void rejectsVaultMountThatContainsOnlySlashes() {
        assertThrows(IllegalArgumentException.class,
                () -> HealthCheckFactory.vaultApiPath("///", "health/check", VaultKvVersion.KV2));
    }

    @Test
    void rejectsVaultSecretPathThatContainsOnlySlashes() {
        assertThrows(IllegalArgumentException.class,
                () -> HealthCheckFactory.vaultApiPath("secret", "///", VaultKvVersion.KV1));
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
