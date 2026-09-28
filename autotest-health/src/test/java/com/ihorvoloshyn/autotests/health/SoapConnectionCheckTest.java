package com.ihorvoloshyn.autotests.health;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class SoapConnectionCheckTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void reportsSuccessfulSoapEndpoint() throws IOException {
        server = startServer(200, true);

        ConnectionCheckResult result =
                new SoapConnectionCheck(url(), null, null).check();

        assertTrue(result.success());
        assertTrue(result.message().contains("200"));
    }

    @Test
    void sendsBasicAuthentication() throws IOException {
        server = startServer(200, true);

        ConnectionCheckResult result =
                new SoapConnectionCheck(url(), "user", "pass").check();

        assertTrue(result.success());
    }

    @Test
    void reportsFailureForNonSuccessfulResponse() throws IOException {
        server = startServer(500, true);

        ConnectionCheckResult result =
                new SoapConnectionCheck(url(), null, null).check();

        assertFalse(result.success());
        assertTrue(result.message().contains("500"));
    }

    @Test
    void reportsFailureWhenEndpointCannotBeReached() {
        ConnectionCheckResult result =
                new SoapConnectionCheck("http://127.0.0.1:1/?wsdl", null, null).check();

        assertFalse(result.success());
        assertFalse(result.message().isBlank());
    }

    @Test
    void rejectsBlankUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> new SoapConnectionCheck("", null, null));
    }

    private HttpServer startServer(int status, boolean verifyAuth) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            if (verifyAuth && exchange.getRequestHeaders().getFirst("Authorization") != null) {
                assertEquals("Basic dXNlcjpwYXNz",
                        exchange.getRequestHeaders().getFirst("Authorization"));
            }
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        return server;
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/?wsdl";
    }
}
