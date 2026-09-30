package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.db.DatabaseType;
import com.ihorvoloshyn.autotests.health.*;
import com.ihorvoloshyn.autotests.infrastructure.CommandExecutor;
import com.ihorvoloshyn.autotests.vault.VaultKvVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds only the health checks explicitly configured in FrameworkConfig.
 * Creating the checks does not execute external calls.
 */
public final class HealthCheckFactory {
    private final FrameworkConfig config;
    private final ConnectionFactory connections;

    public HealthCheckFactory(FrameworkConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
        this.connections = new ConnectionFactory(config);
    }

    public List<ConnectionCheck> createChecks() {
        List<ConnectionCheck> checks = new ArrayList<>();
        addHttp(checks, "REST", "health.rest.url");
        addSoap(checks);
        addVault(checks);
        addDatabase(checks, DatabaseType.POSTGRESQL, "PostgreSQL", "health.postgresql");
        addDatabase(checks, DatabaseType.ORACLE, "Oracle", "health.oracle");
        addCamunda(checks);
        addRabbitMq(checks);
        addElk(checks);
        addOkd(checks);
        return List.copyOf(checks);
    }

    public ConnectionHealthService createService() {
        return new ConnectionHealthService(createChecks());
    }

    private void addHttp(List<ConnectionCheck> checks, String name, String key) {
        String url = value(key);
        if (!url.isBlank()) checks.add(new HttpConnectionCheck(name, url));
    }

    private void addSoap(List<ConnectionCheck> checks) {
        String url = value("health.soap.url");
        if (url.isBlank()) return;

        String wsdlPath = valueOr("health.soap.wsdl-path", "?wsdl");
        checks.add(new SoapConnectionCheck(
                appendPath(url, wsdlPath),
                value("health.soap.username"),
                value("health.soap.password")));
    }

    private void addVault(List<ConnectionCheck> checks) {
        String url = value("health.vault.url");
        String path = value("health.vault.check.path");
        if (url.isBlank() || path.isBlank()) return;

        parseKvVersion(valueOr("health.vault.kv-version", "KV2"));
        checks.add(new VaultConnectionCheck(
                () -> connections.vault("health.vault"),
                path));
    }

    private void addDatabase(
            List<ConnectionCheck> checks,
            DatabaseType type,
            String name,
            String prefix) {

        boolean hasHost = !value(prefix + ".host").isBlank();
        boolean hasDatabase = !value(prefix + ".database").isBlank();
        boolean hasSchema = !value(prefix + ".schema").isBlank();

        if (!hasHost && !hasDatabase && !hasSchema) return;
        if (!hasHost || !hasDatabase || !hasSchema) {
            throw new IllegalStateException(prefix + " requires host, database and schema");
        }

        checks.add(new DatabaseConnectionCheck(
                name,
                () -> connections.database(prefix, type)));
    }

    private void addCamunda(List<ConnectionCheck> checks) {
        if (!value("health.camunda.url").isBlank()) {
            CamundaClient client = connections.camunda("health.camunda");
            checks.add(new CamundaConnectionCheck(client));
        }
    }

    private void addRabbitMq(List<ConnectionCheck> checks) {
        String endpoint = value("health.rabbitmq.endpoint");
        String url = value("health.rabbitmq.url");
        if (endpoint.isBlank() && url.isBlank()) return;

        checks.add(new RabbitMqConnectionCheck(
                connections.rabbitMq("health.rabbitmq")));
    }

    private void addElk(List<ConnectionCheck> checks) {
        if (!value("health.elk.url").isBlank()) {
            checks.add(new ElkConnectionCheck(connections.elk("health.elk")));
        }
    }

    private void addOkd(List<ConnectionCheck> checks) {
        if (!value("health.okd.namespace").isBlank()) {
            checks.add(new OkdConnectionCheck(new CommandExecutor(), value("health.okd.namespace")));
        }
    }

    private String value(String key) {
        return config.property(key, "");
    }

    private String valueOr(String key, String fallback) {
        String value = value(key);
        return value.isBlank() ? fallback : value;
    }

    private static VaultKvVersion parseKvVersion(String value) {
        try {
            return VaultKvVersion.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported Vault KV version: " + value, e);
        }
    }

    private static String appendPath(String base, String path) {
        if (path == null || path.isBlank()) return base;
        if (base.endsWith("/") && path.startsWith("/")) return base + path.substring(1);
        if (!base.endsWith("/") && !path.startsWith("/")) return base + "/" + path;
        return base + path;
    }
}
