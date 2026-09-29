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

    @Test
    void rejectsNullCheckEntry() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConnectionHealthService(java.util.Arrays.asList((ConnectionCheck) null)));
    }
}

    @Test
    void returnsRunSummaryForSequentialExecution() {
        ConnectionCheck success = () -> ConnectionCheckResult.success("ok", "OK", 1);
        ConnectionCheck failure = () -> ConnectionCheckResult.failure("bad", "FAIL", 2);

        HealthRunResult result = new ConnectionHealthService(List.of(success, failure)).run();

        assertEquals(2, result.total());
        assertEquals(1, result.successful());
        assertEquals(1, result.failed());
        assertFalse(result.success());
    }

    @Test
    void supportsParallelExecution() {
        ConnectionCheck first = () -> {
            sleep(50);
            return ConnectionCheckResult.success("first", "OK", 1);
        };
        ConnectionCheck second = () -> {
            sleep(50);
            return ConnectionCheckResult.success("second", "OK", 1);
        };

        long start = System.nanoTime();
        HealthRunResult result = new ConnectionHealthService(List.of(first, second))
                .run(HealthRunOptions.parallel());
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertTrue(result.success());
        assertEquals(2, result.total());
        assertTrue(elapsed < 150, "Checks should execute concurrently");
    }

    @Test
    void failFastStopsSequentialExecution() {
        int[] calls = {0};
        ConnectionCheck failure = () -> {
            calls[0]++;
            return ConnectionCheckResult.failure("bad", "FAIL", 1);
        };
        ConnectionCheck skipped = () -> {
            calls[0]++;
            return ConnectionCheckResult.success("skipped", "OK", 1);
        };

        HealthRunResult result = new ConnectionHealthService(List.of(failure, skipped))
                .run(new HealthRunOptions(false, true));

        assertEquals(1, calls[0]);
        assertEquals(1, result.total());
        assertFalse(result.success());
    }

    @Test
    void rejectsNullRunOptions() {
        ConnectionHealthService service = new ConnectionHealthService(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.run(null));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

