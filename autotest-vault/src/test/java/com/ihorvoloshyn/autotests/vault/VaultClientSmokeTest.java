package com.ihorvoloshyn.autotests.vault;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultClientSmokeTest {

    @Test
    void rejectsBlankToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VaultClient("http://localhost:8200", ""));
    }
}