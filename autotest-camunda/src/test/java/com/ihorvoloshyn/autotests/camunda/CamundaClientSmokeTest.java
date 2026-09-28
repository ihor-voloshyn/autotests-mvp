package com.ihorvoloshyn.autotests.camunda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamundaClientSmokeTest {

    @Test
    void rejectsBlankBaseUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CamundaClient(""));
    }

    @Test
    void usesDefaultPort8143WhenPortIsNotSpecified() {
        CamundaClient client = new CamundaClient("https://camunda.example.com");
        assertEquals("https://camunda.example.com:8143", client.baseUrl());
    }

    @Test
    void preservesExplicitPort() {
        CamundaClient client = new CamundaClient("https://camunda.example.com:9443");
        assertEquals("https://camunda.example.com:9443", client.baseUrl());
    }
}
