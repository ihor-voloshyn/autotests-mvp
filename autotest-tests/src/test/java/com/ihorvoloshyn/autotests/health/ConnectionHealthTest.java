package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.ihorvoloshyn.autotests.connectors.ConnectionFactory;
import com.ihorvoloshyn.autotests.core.config.*;
import com.ihorvoloshyn.autotests.db.DatabaseType;
import com.ihorvoloshyn.autotests.infrastructure.CommandExecutor;
import com.ihorvoloshyn.autotests.reporting.AllureSupport;
import com.ihorvoloshyn.autotests.vault.VaultClient;
import com.ihorvoloshyn.autotests.vault.VaultKvVersion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionHealthTest {

    @Test
    void allConfiguredConnectionsAreHealthy() {
        FrameworkConfig config = new EnvironmentConfigLoader().load();
        ConnectionFactory factory = new ConnectionFactory(config);
        List<ConnectionCheck> checks = new ArrayList<>();

        SecretResolver secrets = buildSecrets(config);

        addRest(checks, factory, config);
        addSoap(checks, config);
        addVault(checks, factory, config);
        addDatabase(checks, factory, config, "health.postgresql", DatabaseType.POSTGRESQL, secrets);
        addDatabase(checks, factory, config, "health.oracle", DatabaseType.ORACLE, secrets);
        addCamunda(checks, factory, config);
        addRabbitMq(checks, factory, config, secrets);
        addElk(checks, factory, config, secrets);
        addOkd(checks, config);

        if (checks.isEmpty()) {
            AllureSupport.attachText("Connection health", "No health checks are configured.");
            return;
        }

        List<ConnectionCheckResult> results = new ConnectionHealthService(checks).checkAll();
        String report = results.stream()
                .map(r -> "%s | %s | %d ms | %s".formatted(
                        r.success() ? "PASS" : "FAIL", r.name(), r.durationMs(), r.message()))
                .reduce((a, b) -> a + System.lineSeparator() + b)
                .orElse("No results");

        AllureSupport.attachText("Connection health", report);

        List<String> failures = results.stream()
                .filter(r -> !r.success())
                .map(r -> r.name() + ": " + r.message())
                .toList();

        assertTrue(failures.isEmpty(), () -> "Connection health failures: " + failures);
    }

    private static SecretResolver buildSecrets(FrameworkConfig config) {
        SecretResolver configResolver = new ConfigurationSecretResolver(config);
        String url = config.property("health.vault.url", "");
        String authPath = config.property("health.vault.auth.path", "");
        String user = config.property("health.vault.username", "");
        String path = config.property("health.vault.check.path", "");
        String mount = config.property("health.vault.mount", "secret");
        VaultKvVersion version = VaultKvVersion.valueOf(
                config.property("health.vault.kv-version", "KV2").toUpperCase());

        if (url.isBlank() || authPath.isBlank() || user.isBlank() || path.isBlank()) {
            return configResolver;
        }

        SecretResolver vaultResolver = new LazySecretResolver(() -> {
            ConnectionFactory factory = new ConnectionFactory(config);
            VaultClient client = factory.vault("health.vault");
            return new com.ihorvoloshyn.autotests.vault.VaultSecretResolver(
                    new com.ihorvoloshyn.autotests.vault.VaultSecretStore(client, mount, version), path);
        });

        return new CompositeSecretResolver(List.of(configResolver, vaultResolver));
    }

    private static void addRest(List<ConnectionCheck> checks, ConnectionFactory factory, FrameworkConfig c) {
        String url = c.property("health.rest.url", "");
        if (!url.isBlank()) {
            checks.add(new HttpConnectionCheck("REST", url));
        }
    }

    private static void addSoap(List<ConnectionCheck> checks, FrameworkConfig factoryConfig) {
        String url = factoryConfig.property("health.soap.url", "");
        if (!url.isBlank()) {
            String wsdl = factoryConfig.property("health.soap.wsdl-path", "?wsdl");
            checks.add(new SoapConnectionCheck(join(url, wsdl),
                    factoryConfig.property("health.soap.username", ""),
                    factoryConfig.property("health.soap.password", "")));
        }
    }

    private static void addVault(List<ConnectionCheck> checks, ConnectionFactory factory, FrameworkConfig c) {
        String url = c.property("health.vault.url", "");
        String authPath = c.property("health.vault.auth.path", "");
        String user = c.property("health.vault.username", "");
        String path = c.property("health.vault.check.path", "");
        if (url.isBlank() || authPath.isBlank() || user.isBlank() || path.isBlank()) {
            return;
        }

        checks.add(new VaultConnectionCheck(() -> factory.vault("health.vault"), path));
    }

    private static void addDatabase(
            List<ConnectionCheck> checks,
            ConnectionFactory factory,
            FrameworkConfig c,
            String prefix,
            DatabaseType type,
            SecretResolver secrets) {

        String host = c.property(prefix + ".host", "");
        String database = c.property(prefix + ".database", "");
        String schema = c.property(prefix + ".schema", "");

        if (host.isBlank() && database.isBlank() && schema.isBlank()) {
            return;
        }
        if (host.isBlank() || database.isBlank() || schema.isBlank()) {
            throw new IllegalStateException(prefix + " requires host, database and schema");
        }

        checks.add(new DatabaseConnectionCheck(
                type.name(),
                () -> factory.database(prefix, type, secrets)));
    }

    private static void addCamunda(List<ConnectionCheck> checks, ConnectionFactory factory, FrameworkConfig c) {
        String url = c.property("health.camunda.url", "");
        if (!url.isBlank()) {
            CamundaClient client = factory.camunda("health.camunda");
            checks.add(new CamundaConnectionCheck(client));
        }
    }

    private static void addRabbitMq(
            List<ConnectionCheck> checks,
            ConnectionFactory factory,
            FrameworkConfig c,
            SecretResolver secrets) {

        String endpoint = c.property("health.rabbitmq.endpoint", "");
        if (!endpoint.isBlank()) {
            checks.add(new RabbitMqConnectionCheck(
                    factory.rabbitMq("health.rabbitmq", secrets)));
        }
    }

    private static void addElk(
            List<ConnectionCheck> checks,
            ConnectionFactory factory,
            FrameworkConfig c,
            SecretResolver secrets) {

        String url = c.property("health.elk.url", "");
        if (!url.isBlank()) {
            checks.add(new ElkConnectionCheck(
                    factory.elk("health.elk", secrets)));
        }
    }

    private static void addOkd(List<ConnectionCheck> checks, FrameworkConfig c) {
        String namespace = c.property("health.okd.namespace", "");
        if (!namespace.isBlank()) {
            checks.add(new OkdConnectionCheck(new CommandExecutor(), namespace));
        }
    }

    private static String join(String url, String path) {
        if (path == null || path.isBlank()) return url;
        if (path.startsWith("?")) return url + path;
        return url.replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
    }
}
