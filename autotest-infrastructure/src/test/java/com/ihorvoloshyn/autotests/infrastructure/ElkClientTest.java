package com.ihorvoloshyn.autotests.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ElkClientTest {

    @Test
    void rejectsBlankUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ElkClient(""));
    }
}