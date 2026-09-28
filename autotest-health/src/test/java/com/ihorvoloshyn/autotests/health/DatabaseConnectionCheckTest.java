package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.db.DatabaseHealthClient;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionCheckTest {

    @Test
    void reportsSuccessWhenConnectionAndSchemaAreValid() {
        ConnectionCheckResult result = new DatabaseConnectionCheck(
                "PostgreSQL",
                () -> new StubDatabase(true, true)).check();

        assertTrue(result.success());
        assertEquals("PostgreSQL", result.name());
        assertTrue(result.message().contains("schema"));
        assertTrue(result.durationMillis() >= 0);
    }

    @Test
    void reportsFailureWhenConnectionIsInvalid() {
        ConnectionCheckResult result = new DatabaseConnectionCheck(
                "PostgreSQL",
                () -> new StubDatabase(false, true)).check();

        assertFalse(result.success());
        assertTrue(result.message().contains("validation"));
    }

    @Test
    void reportsFailureWhenSchemaIsNotAccessible() {
        ConnectionCheckResult result = new DatabaseConnectionCheck(
                "Oracle",
                () -> new StubDatabase(true, false)).check();

        assertFalse(result.success());
        assertTrue(result.message().contains("schema"));
    }

    @Test
    void createsClientLazilyAndOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();

        var check = new DatabaseConnectionCheck(
                "PostgreSQL",
                () -> {
                    calls.incrementAndGet();
                    return new StubDatabase(true, true);
                });

        assertEquals(0, calls.get());
        assertTrue(check.check().success());
        assertTrue(check.check().success());
        assertEquals(1, calls.get());
    }

    @Test
    void convertsSupplierExceptionToFailure() {
        var check = new DatabaseConnectionCheck(
                "PostgreSQL",
                () -> {
                    throw new IllegalStateException("database unavailable");
                });

        ConnectionCheckResult result = check.check();

        assertFalse(result.success());
        assertTrue(result.message().contains("database unavailable"));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConnectionCheck("", () -> new StubDatabase(true, true)));
        assertThrows(NullPointerException.class,
                () -> new DatabaseConnectionCheck("DB", null));
    }

    private record StubDatabase(boolean valid, boolean schemaAccessible)
            implements DatabaseHealthClient {

        @Override
        public boolean isValid(int timeoutSeconds) {
            assertEquals(5, timeoutSeconds);
            return valid;
        }

        @Override
        public boolean isSchemaAccessible() {
            return schemaAccessible;
        }
    }
}
