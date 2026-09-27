package com.ihorvoloshyn.autotests.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcClientSmokeTest {

    @Test
    void rejectsBlankJdbcUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JdbcClient("", "user", "password"));
    }
}