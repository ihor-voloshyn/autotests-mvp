package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.infrastructure.CommandExecutor;
import com.ihorvoloshyn.autotests.infrastructure.CommandResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OkdConnectionCheckTest {

    @Test
    void reportsSuccessfulNamespaceCheck() {
        CommandExecutor executor = new StubExecutor(
                new CommandResult(0, "pod/example", ""));

        ConnectionCheckResult result =
                new OkdConnectionCheck(executor, "test").check();

        assertTrue(result.success());
        assertEquals("OKD", result.name());
        assertTrue(result.message().contains("test"));
    }

    @Test
    void reportsFailureWhenOcCommandFails() {
        CommandExecutor executor = new StubExecutor(
                new CommandResult(1, "Forbidden", ""));

        ConnectionCheckResult result =
                new OkdConnectionCheck(executor, "test").check();

        assertFalse(result.success());
        assertTrue(result.message().contains("Forbidden"));
    }

    @Test
    void convertsCommandExceptionToFailure() {
        CommandExecutor executor = new StubExecutor(null) {
            @Override
            public CommandResult execute(String... command) {
                throw new IllegalStateException("oc is not installed");
            }
        };

        ConnectionCheckResult result =
                new OkdConnectionCheck(executor, "test").check();

        assertFalse(result.success());
        assertTrue(result.message().contains("oc is not installed"));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new OkdConnectionCheck(null, "test"));
        assertThrows(IllegalArgumentException.class,
                () -> new OkdConnectionCheck(new StubExecutor(
                        new CommandResult(0, "", "")), ""));
    }

    private static class StubExecutor extends CommandExecutor {
        private final CommandResult result;

        private StubExecutor(CommandResult result) {
            this.result = result;
        }

        @Override
        public CommandResult execute(String... command) {
            return result;
        }

        @Override
        public CommandResult execute(List<String> command, Duration timeout) {
            return result;
        }
    }
}
