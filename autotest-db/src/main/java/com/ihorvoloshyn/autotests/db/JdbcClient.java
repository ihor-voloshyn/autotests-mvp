package com.ihorvoloshyn.autotests.db;

import java.sql.*;
import java.util.*;

public class JdbcClient implements AutoCloseable {

    private final Connection connection;

    public JdbcClient(String jdbcUrl, String username, String password) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be blank");
        }
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to database: " + jdbcUrl, e);
        }
    }

    public List<Map<String, Object>> query(String sql, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return readRows(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed", e);
        }
    }

    public int update(String sql, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Database update failed", e);
        }
    }

    public boolean isValid(int timeoutSeconds) {
        try {
            return connection.isValid(timeoutSeconds);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot close database connection", e);
        }
    }

    private static void bind(PreparedStatement statement, Object[] parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private static List<Map<String, Object>> readRows(ResultSet resultSet)
            throws SQLException {
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