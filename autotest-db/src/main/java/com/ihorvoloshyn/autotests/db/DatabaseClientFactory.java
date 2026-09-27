package com.ihorvoloshyn.autotests.db;

public final class DatabaseClientFactory {

    private DatabaseClientFactory() {
    }

    public static JdbcClient create(
            DatabaseType type,
            String host,
            int port,
            String database,
            String username,
            String password) {

        if (type == null) {
            throw new IllegalArgumentException("database type must not be null");
        }

        String url = switch (type) {
            case POSTGRESQL ->
                    "jdbc:postgresql://" + host + ":" + port + "/" + database;
            case ORACLE ->
                    "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database;
        };

        return new JdbcClient(url, username, password);
    }
}