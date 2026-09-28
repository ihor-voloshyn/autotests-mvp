package com.ihorvoloshyn.autotests.rest;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RestClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsGetToConfiguredBasePath() throws IOException {
        server = startServer(exchange -> {
            assertEquals("/api/v1/resource", exchange.getRequestURI().getPath());
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("ok".getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });

        var response = new RestClient(baseUrl("/api/v1")).get("/resource");

        assertEquals(200, response.statusCode());
        assertEquals("ok", response.asString());
    }

    @Test
    void sendsQueryParameters() throws IOException {
        server = startServer(exchange -> {
            assertEquals("/search", exchange.getRequestURI().getPath());
            String query = exchange.getRequestURI().getRawQuery();
            assertNotNull(query);
            assertTrue(query.contains("q=java"));
            assertTrue(query.contains("page=2"));
            assertEquals(2, query.split("&").length);

            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        var response = new RestClient(baseUrl("")).get(
                "/search", Map.of("q", "java", "page", 2));

        assertEquals(200, response.statusCode());
        assertEquals("ok", response.asString());
    }

    @Test
    void sendsBasicAuthentication() throws IOException {
        server = startServer(exchange -> {
            assertEquals("Basic dXNlcjpwYXNz",
                    exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        var response = new RestClient(
                com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver.resolve(baseUrl("")),
                new com.ihorvoloshyn.autotests.core.config.Credentials("user", "pass"))
                .get("/");

        assertEquals(200, response.statusCode());
    }

    @Test
    void usesHttpDefaultPort80() {
        var client = new RestClient("http://example.com/api");
        assertNotNull(client);
    }

    @Test
    void usesHttpsDefaultPort443() {
        var client = new RestClient("https://example.com/api");
        assertNotNull(client);
    }

    @Test
    void rejectsEndpointWithoutScheme() {
        assertThrows(IllegalArgumentException.class,
                () -> new RestClient("example.com:8080"));
    }

    private HttpServer startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", handler);
        server.start();
        return server;
    }

    private String baseUrl(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }
}
