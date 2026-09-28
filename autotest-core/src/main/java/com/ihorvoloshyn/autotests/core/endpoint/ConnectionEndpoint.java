package com.ihorvoloshyn.autotests.core.endpoint;

import java.net.URI;

public record ConnectionEndpoint(String scheme, String host, Integer port, String path) {
    public ConnectionEndpoint {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port != null && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        scheme = scheme == null || scheme.isBlank() ? null : scheme.toLowerCase();
        path = path == null || path.isBlank() ? null : path;
    }

    public ConnectionEndpoint(String host, Integer port) {
        this(null, host, port, null);
    }

    public boolean hasPort() {
        return port != null;
    }

    public int portOr(int defaultPort) {
        return port == null ? defaultPort : port;
    }

    public ConnectionEndpoint withDefaultPort(int defaultPort) {
        if (defaultPort < 1 || defaultPort > 65535) {
            throw new IllegalArgumentException("defaultPort must be between 1 and 65535");
        }
        return port == null
                ? new ConnectionEndpoint(scheme, host, defaultPort, path)
                : this;
    }

    public String authority() {
        String h = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        return port == null ? h : h + ":" + port;
    }

    public URI toUri() {
        if (scheme == null) {
            throw new IllegalStateException("Cannot create URI without scheme");
        }
        return URI.create(scheme + "://" + authority()
                + (path == null ? "" : (path.startsWith("/") ? path : "/" + path)));
    }
}
