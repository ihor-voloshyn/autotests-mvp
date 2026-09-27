package com.ihorvoloshyn.autotests.core.config;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;

public record ServiceConfig(
        String name,
        ConnectionEndpoint endpoint,
        Credentials credentials) {

    public ServiceConfig {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        credentials = credentials == null ? Credentials.empty() : credentials;
    }

    public ServiceConfig(String name, ConnectionEndpoint endpoint, String username, String password) {
        this(name, endpoint, new Credentials(username, password));
    }

    public static ServiceConfig from(FrameworkConfig config, String prefix, String name) {
        String endpoint = config.requiredProperty(prefix + ".url");
        return new ServiceConfig(
                name,
                EndpointResolver.resolve(endpoint),
                config.property(prefix + ".username", ""),
                config.property(prefix + ".password", ""));
    }

    public static ServiceConfig from(FrameworkConfig config, String prefix, String name, SecretResolver secrets) {
        if (secrets == null) throw new IllegalArgumentException("secrets must not be null");
        String endpoint = config.requiredProperty(prefix + ".url");
        String usernameKey = config.property(prefix + ".username-key", prefix + ".username");
        String passwordKey = config.property(prefix + ".password-key", prefix + ".password");
        return new ServiceConfig(
                name,
                EndpointResolver.resolve(endpoint),
                new Credentials(secrets.resolve(usernameKey), secrets.resolve(passwordKey)));
    }

    public String username() {
        return credentials.username();
    }

    public String password() {
        return credentials.password();
    }

    public boolean hasCredentials() {
        return credentials.isConfigured();
    }
}
