package com.ihorvoloshyn.autotests.core.endpoint;

import java.net.URI;

public final class EndpointResolver {
    private EndpointResolver() {}

    public static ConnectionEndpoint resolve(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Endpoint must not be blank");
        String input = value.trim();

        if (input.contains("://")) {
            URI uri = URI.create(input);
            if (uri.getHost() == null || uri.getHost().isBlank()) throw new IllegalArgumentException("Endpoint URL has no valid host: " + value);
            return new ConnectionEndpoint(uri.getScheme(), uri.getHost(), uri.getPort() == -1 ? null : uri.getPort(), uri.getRawPath());
        }

        String host = input;
        Integer port = null;
        if (input.startsWith("[")) {
            int close = input.indexOf(']');
            if (close < 0) throw new IllegalArgumentException("Invalid IPv6 endpoint: " + value);
            host = input.substring(1, close);
            if (input.length() > close + 1) {
                if (input.charAt(close + 1) != ':') throw new IllegalArgumentException("Invalid IPv6 endpoint: " + value);
                port = parsePort(input.substring(close + 2), value);
            }
        } else {
            int colon = input.lastIndexOf(':');
            if (colon > 0 && input.indexOf(':') == colon) {
                host = input.substring(0, colon);
                port = parsePort(input.substring(colon + 1), value);
            }
        }
        return new ConnectionEndpoint(null, host, port, null);
    }

    private static int parsePort(String value, String original) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port in endpoint: " + original, e);
        }
    }
}