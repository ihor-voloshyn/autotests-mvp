package com.ihorvoloshyn.autotests.infrastructure;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.nio.file.Path;
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
    void capturesProcessOutput() {
        String executable = System.getProperty("os.name").toLowerCase().contains("win")
                ? "java.exe"
                : "java";
        String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();

        var result = new CommandExecutor().execute(List.of(java, "-version"), Duration.ofSeconds(10));

        assertTrue(result.success(), result.error());
        assertTrue(result.output().toLowerCase().contains("version"), result.output());
    }

    @Test
    void rejectsEmptyCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CommandExecutor().execute(List.of(), Duration.ofSeconds(1)));
    }
}