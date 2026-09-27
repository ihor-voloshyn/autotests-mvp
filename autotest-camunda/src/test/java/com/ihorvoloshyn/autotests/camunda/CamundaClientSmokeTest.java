package com.ihorvoloshyn.autotests.camunda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CamundaClientSmokeTest {

    @Test
    void rejectsBlankBaseUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CamundaClient(""));
    }
}