package com.ihorvoloshyn.autotests.core.config;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LazySecretResolverTest {

    @Test
    void supplierIsNotCalledUntilFirstResolve() {
        AtomicInteger calls = new AtomicInteger();

        SecretResolver resolver = new LazySecretResolver(() -> {
            calls.incrementAndGet();
            return SecretResolver.fromMap(java.util.Map.of("key", "value"));
        });

        assertEquals(0, calls.get());
        assertEquals("value", resolver.resolve("key"));
        assertEquals(1, calls.get());
        assertEquals("value", resolver.resolve("key"));
        assertEquals(1, calls.get());
    }

    @Test
    void rejectsNullSupplier() {
        assertThrows(NullPointerException.class, () -> new LazySecretResolver(null));
    }
}
