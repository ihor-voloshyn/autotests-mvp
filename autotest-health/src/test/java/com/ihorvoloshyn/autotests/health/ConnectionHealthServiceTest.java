package com.ihorvoloshyn.autotests.health;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionHealthServiceTest {

    @Test
    void executesAllChecksAndPreservesResults() {
        ConnectionCheck first = () -> ConnectionCheckResult.success("first", "ok", 1);
        ConnectionCheck second = () -> ConnectionCheckResult.failure("second", "failed", 2);

        List<ConnectionCheckResult> results =
                new ConnectionHealthService(List.of(first, second)).checkAll();

        assertEquals(2, results.size());
        assertTrue(results.get(0).success());
        assertFalse(results.get(1).success());
    }

    @Test
    void convertsThrownExceptionToFailureResult() {
        ConnectionCheck failing = () -> {
            throw new IllegalStateException("boom");
        };

        ConnectionCheckResult result =
                new ConnectionHealthService(List.of(failing)).checkAll().getFirst();

        assertFalse(result.success());
        assertTrue(result.message().contains("boom"));
    }

    @Test
    void rejectsNullChecks() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConnectionHealthService(null));
    }
}
