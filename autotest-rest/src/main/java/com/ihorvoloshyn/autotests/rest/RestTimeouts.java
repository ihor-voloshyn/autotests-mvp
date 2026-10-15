package com.ihorvoloshyn.autotests.rest;

import com.ihorvoloshyn.autotests.core.config.FrameworkConfig;

/**
 * HTTP timeout values, expressed in milliseconds.
 */
public record RestTimeouts(int connectionRequestMillis, int connectMillis, int responseMillis) {
    public static final String CONNECTION_REQUEST_PROPERTY = "rest.timeout.connection-request-ms";
    public static final String CONNECT_PROPERTY = "rest.timeout.connect-ms";
    public static final String RESPONSE_PROPERTY = "rest.timeout.response-ms";

    public RestTimeouts {
        requirePositive(CONNECTION_REQUEST_PROPERTY, connectionRequestMillis);
        requirePositive(CONNECT_PROPERTY, connectMillis);
        requirePositive(RESPONSE_PROPERTY, responseMillis);
    }

    public static RestTimeouts from(FrameworkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("FrameworkConfig must not be null");
        }
        return new RestTimeouts(
                readMillis(config, CONNECTION_REQUEST_PROPERTY, 5_000),
                readMillis(config, CONNECT_PROPERTY, 5_000),
                readMillis(config, RESPONSE_PROPERTY, 10_000));
    }

    private static int readMillis(FrameworkConfig config, String key, int fallback) {
        String value = config.property(key, Integer.toString(fallback));
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Configuration property must be a positive integer: " + key, e);
        }
    }

    private static void requirePositive(String key, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Configuration property must be greater than zero: " + key);
        }
    }
}
