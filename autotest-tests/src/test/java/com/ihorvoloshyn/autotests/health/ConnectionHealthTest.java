package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.core.config.*;
import com.ihorvoloshyn.autotests.db.*;
import com.ihorvoloshyn.autotests.infrastructure.CommandExecutor;
import com.ihorvoloshyn.autotests.messaging.RabbitMqClient;
import com.ihorvoloshyn.autotests.reporting.AllureSupport;
import com.ihorvoloshyn.autotests.vault.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionHealthTest {

    @Test
    void allConfiguredConnectionsAreHealthy() {
        FrameworkConfig config = new EnvironmentConfigLoader().load();
        List<ConnectionCheck> checks = new ArrayList<>();

        addHttp(checks, config, "health.rest.url", "REST");
        addSoap(checks, config);
        addVault(checks, config);
        addDatabase(checks, config, "health.postgresql", DatabaseType.POSTGRESQL);
        addDatabase(checks, config, "health.oracle", DatabaseType.ORACLE);
        addCamunda(checks, config);
        addRabbitMq(checks, config);
        addElk(checks, config);
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

    private static void addHttp(List<ConnectionCheck> checks, FrameworkConfig c, String key, String name) {
        String url = c.property(key, "");
        if (!url.isBlank()) checks.add(new HttpConnectionCheck(name, url));
    }

    private static void addSoap(List<ConnectionCheck> checks, FrameworkConfig c) {
        String url = c.property("health.soap.url", "");
        if (!url.isBlank()) {
            String wsdl = c.property("health.soap.wsdl-path", "?wsdl");
            checks.add(new SoapConnectionCheck(join(url, wsdl),
                    c.property("health.soap.username", ""),
                    c.property("health.soap.password", "")));
        }
    }

    private static void addVault(List<ConnectionCheck> checks, FrameworkConfig c) {
        String url = c.property("health.vault.url", "");
        String authPath = c.property("health.vault.auth.path", "");
        String user = c.property("health.vault.username", "");
        if (url.isBlank() || authPath.isBlank() || user.isBlank()) return;
        String path = c.property("health.vault.check.path", "");
        if (path.isBlank()) return;
        VaultAuthenticator auth = new BasicAuthVaultAuthenticator(url, authPath, user,
                c.property("health.vault.password", ""));
        VaultClient client = new AuthenticatedVaultClient(url, auth).authenticate();
        checks.add(new VaultConnectionCheck(client, path));
    }

    private static void addDatabase(List<ConnectionCheck> checks, FrameworkConfig c, String prefix, DatabaseType type) {
        String host = c.property(prefix + ".host", "");
        String database = c.property(prefix + ".database", "");
        String schema = c.property(prefix + ".schema", "");
        if (host.isBlank() && database.isBlank() && schema.isBlank()) return;
        if (host.isBlank() || database.isBlank() || schema.isBlank()) {
            throw new IllegalStateException(prefix + " requires host, database and schema");
        }
        int port = Integer.parseInt(c.property(prefix + ".port", type == DatabaseType.POSTGRESQL ? "5432" : "1521"));
        DatabaseEndpoint endpoint = new DatabaseEndpoint(type, host, port, database, schema,
                c.property(prefix + ".username", ""), c.property(prefix + ".password", ""));
        checks.add(new DatabaseConnectionCheck(type.name(), DatabaseClientFactory.create(endpoint)));
    }

    private static void addCamunda(List<ConnectionCheck> checks, FrameworkConfig c) {
        String url = c.property("health.camunda.url", "");
        if (!url.isBlank()) checks.add(new CamundaConnectionCheck(
                new com.ihorvoloshyn.autotests.camunda.CamundaClient(url)));
    }

    private static void addRabbitMq(List<ConnectionCheck> checks, FrameworkConfig c) {
        String endpoint = c.property("health.rabbitmq.endpoint", "");
        if (!endpoint.isBlank()) {
            checks.add(new RabbitMqConnectionCheck(new RabbitMqClient(endpoint,
                    c.property("health.rabbitmq.username", ""),
                    c.property("health.rabbitmq.password", ""),
                    c.property("health.rabbitmq.virtual-host", "/")));
        }
    }

    private static void addElk(List<ConnectionCheck> checks, FrameworkConfig c) {
        String url = c.property("health.elk.url", "");
        if (!url.isBlank()) checks.add(new ElkConnectionCheck(url));
    }

    private static void addOkd(List<ConnectionCheck> checks, FrameworkConfig c) {
        String namespace = c.property("health.okd.namespace", "");
        if (!namespace.isBlank()) checks.add(new OkdConnectionCheck(new CommandExecutor(), namespace));
    }

    private static String join(String url, String path) {
        if (path == null || path.isBlank()) return url;
        if (path.startsWith("?")) return url + path;
        return url.replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
    }
}
