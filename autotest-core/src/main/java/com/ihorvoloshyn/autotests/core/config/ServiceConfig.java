package com.ihorvoloshyn.autotests.core.config;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;

public record ServiceConfig(
        String name,
        ConnectionEndpoint endpoint,
        String username,
        String password) {

    public ServiceConfig {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        username = username == null ? "" : username;
        password = password == null ? "" : password;
    }

    public static ServiceConfig from(FrameworkConfig config, String prefix, String name) {
        String endpoint = config.requiredProperty(prefix + ".url");
        return new ServiceConfig(
                name,
                EndpointResolver.resolve(endpoint),
                config.property(prefix + ".username", ""),
                config.property(prefix + ".password", ""));
    }

    public boolean hasCredentials() {
        return !username.isBlank();
    }
}
