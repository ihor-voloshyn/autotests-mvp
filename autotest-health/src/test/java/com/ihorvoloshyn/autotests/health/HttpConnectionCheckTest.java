package com.ihorvoloshyn.autotests.health;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class HttpConnectionCheckTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void reportsSuccessful2xxResponse() throws IOException {
        server = startServer(200);

        ConnectionCheckResult result =
                new HttpConnectionCheck("test-http", url()).check();

        assertTrue(result.success());
        assertEquals("test-http", result.name());
        assertTrue(result.message().contains("200"));
        assertTrue(result.durationMs() >= 0);
    }

    @Test
    void reportsSuccessful3xxResponse() throws IOException {
        server = startServer(302);

        ConnectionCheckResult result =
                new HttpConnectionCheck("test-http", url()).check();

        assertTrue(result.success());
        assertTrue(result.message().contains("302"));
    }

    @Test
    void reportsFailureFor4xxResponse() throws IOException {
        server = startServer(404);

        ConnectionCheckResult result =
                new HttpConnectionCheck("test-http", url()).check();

        assertFalse(result.success());
        assertTrue(result.message().contains("404"));
    }

    @Test
    void reportsFailureWhenEndpointCannotBeReached() {
        ConnectionCheckResult result =
                new HttpConnectionCheck("test-http", "http://127.0.0.1:1").check();

        assertFalse(result.success());
        assertFalse(result.message().isBlank());
    }

    @Test
    void rejectsBlankArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new HttpConnectionCheck("", "http://localhost"));
        assertThrows(IllegalArgumentException.class,
                () -> new HttpConnectionCheck("test", ""));
    }

    private HttpServer startServer(int status) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        return server;
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }
}
