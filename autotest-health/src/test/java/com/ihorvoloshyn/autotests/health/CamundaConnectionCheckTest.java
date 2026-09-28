package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.camunda.CamundaClient;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class CamundaConnectionCheckTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void reportsSuccessfulCamundaResponse() throws IOException {
        server = startServer(200);
        var result = new CamundaConnectionCheck(
                new CamundaClient(url())).check();

        assertTrue(result.success());
        assertEquals("Camunda", result.name());
    }

    @Test
    void reportsFailureForCamundaErrorResponse() throws IOException {
        server = startServer(503);
        var result = new CamundaConnectionCheck(
                new CamundaClient(url())).check();

        assertFalse(result.success());
        assertTrue(result.message().contains("503"));
    }

    @Test
    void rejectsNullClient() {
        assertThrows(IllegalArgumentException.class,
                () -> new CamundaConnectionCheck(null));
    }

    private HttpServer startServer(int status) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/engine-rest/engine", exchange -> {
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
