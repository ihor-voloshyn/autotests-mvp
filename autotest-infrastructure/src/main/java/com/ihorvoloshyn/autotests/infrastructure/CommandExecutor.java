package com.ihorvoloshyn.autotests.infrastructure;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandExecutor {

    public CommandResult execute(List<String> command, Duration timeout) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new CommandResult(-1, "", "Command timed out: " + String.join(" ", command));
            }

            String output = new String(process.getInputStream().readAllBytes());
            return new CommandResult(process.exitValue(), output, "");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute command: " + command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command execution interrupted", e);
        }
    }

    public CommandResult execute(String... command) {
        return execute(List.of(command), Duration.ofSeconds(30));
    }
}