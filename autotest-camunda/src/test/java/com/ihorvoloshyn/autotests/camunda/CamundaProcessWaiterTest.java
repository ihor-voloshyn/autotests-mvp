package com.ihorvoloshyn.autotests.camunda;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CamundaProcessWaiterTest {

    private CamundaProcessWaiter waiter() {
        return new CamundaProcessWaiter(
                new CamundaProcessClient(new CamundaClient("http://localhost")));
    }

    @Test
    void rejectsMissingClient() {
        assertThrows(IllegalArgumentException.class, () -> new CamundaProcessWaiter(null));
    }

    @Test
    void rejectsBlankInstanceId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> waiter().waitForInstance(
                        "",
                        response -> true,
                        Duration.ofSeconds(1),
                        Duration.ofMillis(10)));
    }

    @Test
    void rejectsNullCondition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> waiter().waitForInstance(
                        "instance-1",
                        null,
                        Duration.ofSeconds(1),
                        Duration.ofMillis(10)));
    }

    @Test
    void rejectsZeroOrNegativeTimeout() {
        CamundaProcessWaiter waiter = waiter();

        assertThrows(
                IllegalArgumentException.class,
                () -> waiter.waitForInstance(
                        "instance-1",
                        response -> true,
                        Duration.ZERO,
                        Duration.ofMillis(10)));
        assertThrows(
                IllegalArgumentException.class,
                () -> waiter.waitForInstance(
                        "instance-1",
                        response -> true,
                        Duration.ofSeconds(-1),
                        Duration.ofMillis(10)));
    }

    @Test
    void rejectsZeroOrNegativePollInterval() {
        CamundaProcessWaiter waiter = waiter();

        assertThrows(
                IllegalArgumentException.class,
                () -> waiter.waitForInstance(
                        "instance-1",
                        response -> true,
                        Duration.ofSeconds(1),
                        Duration.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> waiter.waitForInstance(
                        "instance-1",
                        response -> true,
                        Duration.ofSeconds(1),
                        Duration.ofMillis(-1)));
    }
}
