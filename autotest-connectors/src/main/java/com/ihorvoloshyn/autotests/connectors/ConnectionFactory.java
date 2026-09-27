package com.ihorvoloshyn.autotests.connectors;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.ihorvoloshyn.autotests.core.config.Credentials;
import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;
import com.ihorvoloshyn.autotests.core.config.SecretResolver;
import com.ihorvoloshyn.autotests.core.config.ServiceConfig;
import com.ihorvoloshyn.autotests.db.DatabaseClientFactory;
import com.ihorvoloshyn.autotests.db.DatabaseEndpoint;
import com.ihorvoloshyn.autotests.db.DatabaseType;
import com.ihorvoloshyn.autotests.db.JdbcClient;
import com.ihorvoloshyn.autotests.infrastructure.ElkClient;
import com.ihorvoloshyn.autotests.messaging.RabbitMqClient;
import com.ihorvoloshyn.autotests.rest.RestClient;
import com.ihorvoloshyn.autotests.soap.SoapClientFactory;
import com.ihorvoloshyn.autotests.vault.AuthenticatedVaultClient;
import com.ihorvoloshyn.autotests.vault.BasicAuthVaultAuthenticator;
import com.ihorvoloshyn.autotests.vault.VaultClient;

public final class ConnectionFactory {
    private final FrameworkConfig config;
    private final java.util.concurrent.ConcurrentHashMap<String, VaultClient> vaultClients =
            new java.util.concurrent.ConcurrentHashMap<>();

    public ConnectionFactory(FrameworkConfig config) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        this.config = config;
    }

    public RestClient rest() {\n        return new RestClient(config.baseUrl());\n    }\n\n    public CamundaClient camunda() {\n        String url = config.property("camunda.url", config.property("camunda.base-url", config.baseUrl()));\n        return new CamundaClient(url);\n    }\n\n    public RestClient rest(String prefix) {
        return rest(prefix, SecretResolver.fromMap(config.properties()));
    }

    public RestClient rest(String prefix, SecretResolver secrets) {
        ServiceConfig service = ServiceConfig.from(config, prefix, prefix, secrets);
        return new RestClient(service.endpoint(), service.credentials());
    }

    public CamundaClient camunda(String prefix) {
        return new CamundaClient(ServiceConfig.from(config, prefix, prefix).endpoint());
    }

    public ElkClient elk(String prefix, SecretResolver secrets) {
        ServiceConfig service = ServiceConfig.from(config, prefix, prefix, secrets);
        String authorization = service.hasCredentials() ? basicAuthorization(service.credentials()) : "";
        return new ElkClient(service.endpoint(), authorization);
    }

    public JdbcClient database(String prefix, DatabaseType type, SecretResolver secrets) {
        return DatabaseClientFactory.create(DatabaseEndpoint.from(config, prefix, type, secrets));
    }

    public RabbitMqClient rabbitMq(String prefix, SecretResolver secrets) {
        String url = config.property(prefix + ".url", "");
        if (url.isBlank()) {
            url = config.requiredProperty(prefix + ".endpoint");
        }
        String usernameKey = configuredKey(prefix + ".username-key", prefix + ".username");
        String passwordKey = configuredKey(prefix + ".password-key", prefix + ".password");
        Credentials credentials = new Credentials(secrets.resolve(usernameKey), secrets.resolve(passwordKey));
        String virtualHost = config.property(prefix + ".virtual-host", "/");
        return new RabbitMqClient(
                com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver.resolve(url),
                credentials.username(),
                credentials.password(),
                virtualHost);
    }

    private String configuredKey(String key, String fallback) {
        String value = config.property(key, "");
        return value.isBlank() ? fallback : value;
    }

    public VaultClient vault(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix must not be blank");
        }
        return vaultClients.computeIfAbsent(prefix, this::authenticateVault);
    }

    private VaultClient authenticateVault(String prefix) {
        String url = config.requiredProperty(prefix + ".url");
        String authPath = config.requiredProperty(prefix + ".auth.path");
        Credentials credentials = new Credentials(
                config.requiredProperty(prefix + ".username"),
                config.property(prefix + ".password", ""));
        return new AuthenticatedVaultClient(
                url,
                new BasicAuthVaultAuthenticator(url, authPath, credentials.username(), credentials.password()))
                .authenticate();
    }

    public <T> T soap(Class<T> serviceClass, String prefix, SecretResolver secrets) {
        ServiceConfig service = ServiceConfig.from(config, prefix, prefix, secrets);
        return SoapClientFactory.create(serviceClass, service.endpoint(), service.username(), service.password());
    }

    private static String basicAuthorization(Credentials credentials) {
        String raw = credentials.username() + ":" + credentials.password();
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
