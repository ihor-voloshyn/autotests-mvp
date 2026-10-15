package com.ihorvoloshyn.autotests.db;

import java.sql.*;
import java.util.*;

public class JdbcClient implements AutoCloseable, DatabaseHealthClient {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final DatabaseEndpoint endpoint;
    private volatile Connection connection;

    public JdbcClient(String jdbcUrl, String username, String password) {
        this(jdbcUrl, username, password, null);
    }

    public JdbcClient(String jdbcUrl, String username, String password, DatabaseEndpoint endpoint) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be blank");
        }
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.endpoint = endpoint;
    }

    private Connection connection() {
        Connection current = connection;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            current = connection;
            if (current != null) {
                return current;
            }
            try {
                current = DriverManager.getConnection(jdbcUrl, username, password);
                applySchema(current);
                connection = current;
                return current;
            } catch (SQLException e) {
                if (current != null) {
                    try {
                        current.close();
                    } catch (SQLException closeError) {
                        e.addSuppressed(closeError);
                    }
                }
                throw new IllegalStateException("Cannot connect to database: " + jdbcUrl, e);
            }
        }
    }

    private void applySchema(Connection connection) throws SQLException {
        if (endpoint == null) {
            return;
        }
        switch (endpoint.type()) {
            case POSTGRESQL -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET search_path TO " + quoteIdentifier(endpoint.schema()));
                }
            }
            case ORACLE -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER SESSION SET CURRENT_SCHEMA = " + quoteIdentifier(endpoint.schema()));
                }
            }
        }
    }

    private static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    public List<Map<String, Object>> query(String sql, Object... parameters) {
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return readRows(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed", e);
        }
    }

    public int update(String sql, Object... parameters) {
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            bind(statement, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Database update failed", e);
        }
    }

    public boolean isValid(int timeoutSeconds) {
        try {
            return connection().isValid(timeoutSeconds);
        } catch (RuntimeException | SQLException e) {
            return false;
        }
    }

    public boolean isSchemaAccessible() {
        if (endpoint == null) {
            return true;
        }
        try {
            String sql = switch (endpoint.type()) {
                case POSTGRESQL -> "SELECT current_schema()";
                case ORACLE -> "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM dual";
            };
            List<Map<String, Object>> rows = query(sql);
            if (rows.isEmpty()) {
                return false;
            }
            Object value = rows.get(0).values().stream().findFirst().orElse(null);
            return value != null && endpoint.schema().equalsIgnoreCase(value.toString());
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public void close() {
        Connection current = connection;
        if (current == null) {
            return;
        }
        try {
            current.close();
            connection = null;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot close database connection", e);
        }
    }

    private static void bind(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private static List<Map<String, Object>> readRows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metadata = resultSet.getMetaData();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                row.put(metadata.getColumnLabel(i), resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
