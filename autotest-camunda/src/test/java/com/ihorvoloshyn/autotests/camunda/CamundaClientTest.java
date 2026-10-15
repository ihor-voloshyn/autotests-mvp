package com.ihorvoloshyn.autotests.camunda;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CamundaClientTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void encodesProcessDefinitionKeyAsQueryParameter() throws IOException {
        server = startServer(exchange -> {
            assertEquals("/engine-rest/process-definition", exchange.getRequestURI().getPath());
            String rawQuery = exchange.getRequestURI().getRawQuery();
            assertNotNull(rawQuery);
            assertTrue(rawQuery.contains("key="));
            String value = rawQuery.substring(rawQuery.indexOf("key=") + 4);
            assertEquals("payments & loans", URLDecoder.decode(value, StandardCharsets.UTF_8));
            byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        var response = new CamundaClient(baseUrl()).getProcessDefinitions("payments & loans");

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.asString());
    }

    @Test
    void encodesProcessDefinitionKeyForInstancesQuery() throws IOException {
        server = startServer(exchange -> {
            assertEquals("/engine-rest/process-instance", exchange.getRequestURI().getPath());
            String rawQuery = exchange.getRequestURI().getRawQuery();
            assertNotNull(rawQuery);
            assertTrue(rawQuery.contains("processDefinitionKey="));
            String value = rawQuery.substring(rawQuery.indexOf("processDefinitionKey=") + "processDefinitionKey=".length());
            assertEquals("a&b=c", URLDecoder.decode(value, StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        var response = new CamundaClient(baseUrl()).getProcessInstances("a&b=c");

        assertEquals(200, response.statusCode());
    }

    @Test
    void encodesProcessInstanceIdAsSinglePathSegment() throws IOException {
        server = startServer(exchange -> {
            String path = exchange.getRequestURI().getRawPath();
            assertTrue(path.startsWith("/engine-rest/process-instance/"));
            String segment = path.substring("/engine-rest/process-instance/".length());
            assertEquals("instance id+1", URLDecoder.decode(segment, StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        var response = new CamundaClient(baseUrl()).getProcessInstance("instance id+1");

        assertEquals(200, response.statusCode());
    }

    @Test
    void rejectsEndpointWithoutScheme() {
        assertThrows(IllegalArgumentException.class, () -> new CamundaClient("camunda.local:8143"));
    }

    private HttpServer startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", handler);
        server.start();
        return server;
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }
}
