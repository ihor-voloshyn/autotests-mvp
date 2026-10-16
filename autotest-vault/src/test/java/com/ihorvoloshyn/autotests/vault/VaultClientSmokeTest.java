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

    @Test
    void tokenToStringDoesNotExposeValue() {
        VaultToken token = new VaultToken("sample-token-value");

        org.junit.jupiter.api.Assertions.assertFalse(token.toString().contains("sample-token-value"));
        org.junit.jupiter.api.Assertions.assertTrue(token.toString().contains("<redacted>"));
    }
}