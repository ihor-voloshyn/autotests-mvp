package com.ihorvoloshyn.autotests.db;

import java.sql.*;
import java.util.*;

public class JdbcClient implements AutoCloseable {
    private final Connection connection;
    private final DatabaseEndpoint endpoint;

    public JdbcClient(String jdbcUrl, String username, String password) {
        this(jdbcUrl, username, password, null);
    }

    public JdbcClient(String jdbcUrl, String username, String password, DatabaseEndpoint endpoint) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) throw new IllegalArgumentException("jdbcUrl must not be blank");
        this.endpoint = endpoint;
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
            applySchema();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to database: " + jdbcUrl, e);
        }
    }

    private void applySchema() throws SQLException {
        if (endpoint == null) return;
        switch (endpoint.type()) {
            case POSTGRESQL -> { try (Statement s = connection.createStatement()) { s.execute("SET search_path TO " + quoteIdentifier(endpoint.schema())); } }
            case ORACLE -> { try (Statement s = connection.createStatement()) { s.execute("ALTER SESSION SET CURRENT_SCHEMA = " + quoteIdentifier(endpoint.schema())); } }
        }
    }

    private static String quoteIdentifier(String identifier) {
        return """ + identifier.replace(""", """") + """;
    }

    public List<Map<String,Object>> query(String sql, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) { return readRows(resultSet); }
        } catch (SQLException e) { throw new IllegalStateException("Database query failed", e); }
    }

    public int update(String sql, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            return statement.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Database update failed", e); }
    }

    public boolean isValid(int timeoutSeconds) {
        try { return connection.isValid(timeoutSeconds); } catch (SQLException e) { return false; }
    }

    public boolean isSchemaAccessible() {
        if (endpoint == null) return true;
        try {
            String sql = switch (endpoint.type()) {
                case POSTGRESQL -> "SELECT current_schema()";
                case ORACLE -> "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') FROM dual";
            };
            return !query(sql).isEmpty();
        } catch (RuntimeException e) { return false; }
    }

    @Override public void close() {
        try { connection.close(); } catch (SQLException e) { throw new IllegalStateException("Cannot close database connection", e); }
    }

    private static void bind(PreparedStatement s, Object[] p) throws SQLException {
        for (int i=0;i<p.length;i++) s.setObject(i+1,p[i]);
    }
    private static List<Map<String,Object>> readRows(ResultSet rs) throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        ResultSetMetaData md=rs.getMetaData();
        while(rs.next()){
            Map<String,Object> row=new LinkedHashMap<>();
            for(int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnLabel(i),rs.getObject(i));
            rows.add(row);
        }
        return rows;
    }
}