package com.ihorvoloshyn.autotests.core.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompositeSecretResolverTest {
    @Test
    void usesFirstNonBlankValue() {
        SecretResolver resolver = new CompositeSecretResolver(List.of(
                SecretResolver.fromMap(Map.of("password", "")),
                SecretResolver.fromMap(Map.of("password", "vault-secret"))
        ));
        assertEquals("vault-secret", resolver.resolve("password"));
    }

    @Test
    void returnsNullWhenNoResolverHasValue() {
        SecretResolver resolver = new CompositeSecretResolver(
                List.of(SecretResolver.fromMap(Map.of())));
        assertNull(resolver.resolve("missing"));
    }
}
