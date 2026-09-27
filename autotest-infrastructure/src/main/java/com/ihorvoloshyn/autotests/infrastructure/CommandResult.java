package com.ihorvoloshyn.autotests.infrastructure;

public record CommandResult(int exitCode, String output, String error) {

    public boolean success() {
        return exitCode == 0;
    }

    public void requireSuccess() {
        if (!success()) {
            throw new IllegalStateException(
                    "Command failed with exit code " + exitCode + ": " + error + output);
        }
    }
}