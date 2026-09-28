package com.ihorvoloshyn.autotests.infrastructure;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ElkClientTest {

    @Test
    void rejectsBlankUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ElkClient(""));
    }

    @Test
    void usesDefaultPort9200WhenPortIsNotSpecified() throws Exception {
        ElkClient client = new ElkClient("https://example.com", null);

        Field field = ElkClient.class.getDeclaredField("endpoint");
        field.setAccessible(true);

        var endpoint = (com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint)
                field.get(client);

        assertEquals(9200, endpoint.port());
    }

    @Test
    void preservesExplicitPort() throws Exception {
        ElkClient client = new ElkClient("https://example.com:9443", null);

        Field field = ElkClient.class.getDeclaredField("endpoint");
        field.setAccessible(true);

        var endpoint = (com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint)
                field.get(client);

        assertEquals(9443, endpoint.port());
    }
}
