package com.ihorvoloshyn.autotests.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcClientSmokeTest {

    @Test
    void rejectsBlankJdbcUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JdbcClient("", "user", "password"));
    }

    @Test
    void connectionFailureDoesNotExposeJdbcUrlOrCredentials() {
        String jdbcUrl = "jdbc:unregistered://db.example/test?password=top-secret";

        try (JdbcClient client = new JdbcClient(jdbcUrl, "user", "top-secret")) {
            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> client.query("SELECT 1"));

            assertEquals("Cannot connect to database", error.getMessage());
            assertFalse(error.toString().contains(jdbcUrl));
            assertFalse(error.toString().contains("top-secret"));
            assertFalse(error.getCause().toString().contains(jdbcUrl));
            assertFalse(error.getCause().toString().contains("top-secret"));
        }
    }
}