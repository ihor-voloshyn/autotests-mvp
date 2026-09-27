package com.ihorvoloshyn.autotests.infrastructure;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandExecutorTest {

    @Test
    void commandResultReportsSuccess() {
        var result = new CommandResult(0, "ok", "");
        assertTrue(result.success());
        assertDoesNotThrow(result::requireSuccess);
    }

    @Test
    void failedCommandThrows() {
        var result = new CommandResult(1, "output", "error");
        assertFalse(result.success());
        assertThrows(IllegalStateException.class, result::requireSuccess);
    }

    @Test
    void rejectsEmptyCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CommandExecutor().execute(List.of(), Duration.ofSeconds(1)));
    }
}