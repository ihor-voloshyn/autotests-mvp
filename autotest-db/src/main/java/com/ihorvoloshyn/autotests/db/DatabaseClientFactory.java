package com.ihorvoloshyn.autotests.db;

public final class DatabaseClientFactory {
    private DatabaseClientFactory() {}

    public static JdbcClient create(DatabaseEndpoint endpoint) {
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        String host = endpoint.host().contains(":") && !endpoint.host().startsWith("[")
                ? "[" + endpoint.host() + "]" : endpoint.host();
        String url = switch (endpoint.type()) {
            case POSTGRESQL -> "jdbc:postgresql://" + host + ":" + endpoint.effectivePort() + "/" + endpoint.database();
            case ORACLE -> "jdbc:oracle:thin:@//" + host + ":" + endpoint.effectivePort() + "/" + endpoint.database();
        };
        return new JdbcClient(url, endpoint.username(), endpoint.password(), endpoint);
    }
}