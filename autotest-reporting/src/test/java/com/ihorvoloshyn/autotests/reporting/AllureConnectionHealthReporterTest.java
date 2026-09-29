package com.ihorvoloshyn.autotests.reporting;

import com.ihorvoloshyn.autotests.health.ConnectionCheckResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllureConnectionHealthReporterTest {

    @Test
    void reportsSuccessfulAndFailedResults() {
        var reporter = new AllureConnectionHealthReporter();

        assertDoesNotThrow(() -> reporter.report(List.of(
                ConnectionCheckResult.success("REST", "OK", 12),
                ConnectionCheckResult.failure("DB", "Schema unavailable", 25))));
    }

    @Test
    void rejectsNullResults() {
        assertThrows(IllegalArgumentException.class,
                () -> new AllureConnectionHealthReporter().report(null));
    }

    @Test
    void rejectsNullResultEntry() {
        assertThrows(IllegalArgumentException.class,
                () -> new AllureConnectionHealthReporter().report(
                        java.util.Arrays.asList((ConnectionCheckResult) null)));
    }
}
