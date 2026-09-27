package com.ihorvoloshyn.autotests.smoke;

import com.ihorvoloshyn.autotests.rest.RestClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestClientSmokeTest {

    @Test
    void clientCanBeCreated() {
        RestClient client = new RestClient("http://localhost");
        assertNotNull(client);
    }
}