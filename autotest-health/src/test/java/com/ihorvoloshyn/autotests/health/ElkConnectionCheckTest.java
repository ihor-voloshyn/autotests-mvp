package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.infrastructure.ElkClient;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class ElkConnectionCheckTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void reportsSuccessForHealthyElasticsearch() throws IOException {
        server = startServer(200);

        ConnectionCheckResult result =
                new ElkConnectionCheck(new ElkClient(url(), null)).check();

        assertTrue(result.success());
        assertEquals("ELK", result.name());
        assertTrue(result.message().contains("200"));
        assertTrue(result.durationMs() >= 0);
    }

    @Test
    void reportsFailureForNonSuccessfulResponse() throws IOException {
        server = startServer(503);

        ConnectionCheckResult result =
                new ElkConnectionCheck(new ElkClient(url(), null)).check();

        assertFalse(result.success());
        assertTrue(result.message().contains("503"));
    }

    @Test
    void reportsFailureWhenElasticsearchCannotBeReached() {
        ConnectionCheckResult result =
                new ElkConnectionCheck(
                        new ElkClient("http://127.0.0.1:1", null)).check();

        assertFalse(result.success());
        assertFalse(result.message().isBlank());
    }

    @Test
    void appliesAuthorizationHeader() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/_cluster/health", exchange -> {
            assertEquals("Basic dXNlcjpwYXNz",
                    exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        ConnectionCheckResult result =
                new ElkConnectionCheck(
                        new ElkClient(url(), "Basic dXNlcjpwYXNz")).check();

        assertTrue(result.success());
    }

    @Test
    void rejectsNullClient() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElkConnectionCheck(null));
    }

    private HttpServer startServer(int status) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/_cluster/health", exchange -> {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        return server;
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }
}
