package com.ihorvoloshyn.autotests.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Path outputFile = null;
        Process process = null;
        try {
            outputFile = Files.createTempFile("autotest-command-", ".log");
            process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(outputFile.toFile())
                    .start();

            boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                process.waitFor();
                return new CommandResult(
                        -1,
                        Files.readString(outputFile, StandardCharsets.UTF_8),
                        "Command timed out");
            }

            String output = Files.readString(outputFile, StandardCharsets.UTF_8);
            return new CommandResult(process.exitValue(), output, "");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute command", e);
        } catch (InterruptedException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command execution interrupted", e);
        } finally {
            if (outputFile != null) {
                try {
                    Files.deleteIfExists(outputFile);
                } catch (IOException ignored) {
                    // Best-effort cleanup; do not mask the command result or original failure.
                }
            }
        }
    }

    public CommandResult execute(String... command) {
        return execute(List.of(command), Duration.ofSeconds(30));
    }
}
