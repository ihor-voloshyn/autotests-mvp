package com.ihorvoloshyn.autotests.infrastructure;

import java.time.Duration;

public class OkdClient {

    private final CommandExecutor executor;
    private final Duration timeout;

    public OkdClient(CommandExecutor executor) {
        this(executor, Duration.ofSeconds(30));
    }

    public OkdClient(CommandExecutor executor, Duration timeout) {
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        this.executor = executor;
        this.timeout = timeout;
    }

    public CommandResult getPods(String namespace) {
        return execute("oc", "-n", namespace, "get", "pods", "-o", "wide");
    }

    public CommandResult getPodStatus(String namespace, String pod) {
        return execute("oc", "-n", namespace, "get", "pod", pod, "-o", "json");
    }

    public CommandResult getPodLogs(String namespace, String pod) {
        return execute("oc", "-n", namespace, "logs", pod);
    }

    public CommandResult getPodLogs(String namespace, String pod, String container) {
        return execute("oc", "-n", namespace, "logs", pod, "-c", container);
    }

    public CommandResult restartPod(String namespace, String pod) {
        return execute("oc", "-n", namespace, "delete", "pod", pod);
    }

    public CommandResult execute(String... command) {
        CommandResult result = executor.execute(command);
        result.requireSuccess();
        return result;
    }
}