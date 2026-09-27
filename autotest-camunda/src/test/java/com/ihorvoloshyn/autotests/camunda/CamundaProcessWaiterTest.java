package com.ihorvoloshyn.autotests.camunda;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CamundaProcessWaiterTest {

    @Test
    void rejectsMissingArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CamundaProcessWaiter(null));
    }

    @Test
    void rejectsBlankInstanceId() {
        var waiter = new CamundaProcessWaiter(
                new CamundaProcessClient(new CamundaClient("http://localhost")));

        assertThrows(
                IllegalArgumentException.class,
                () -> waiter.waitForInstance(
                        "",
                        response -> true,
                        Duration.ofSeconds(1),
                        Duration.ofMillis(10)));
    }
}