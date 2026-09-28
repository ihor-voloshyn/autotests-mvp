package com.ihorvoloshyn.autotests.health;

import com.ihorvoloshyn.autotests.vault.VaultClient;
import com.ihorvoloshyn.autotests.vault.VaultToken;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class VaultConnectionCheckTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void reportsSuccessfulVaultApiCall() throws IOException {
        server = startServer(200);

        ConnectionCheckResult result = new VaultConnectionCheck(
                new VaultClient(url(), new VaultToken("token")),
                "health").check();

        assertTrue(result.success());
        assertEquals("Vault", result.name());
    }

    @Test
    void reportsFailureForRejectedToken() throws IOException {
        server = startServer(403);

        ConnectionCheckResult result = new VaultConnectionCheck(
                new VaultClient(url(), new VaultToken("token")),
                "health").check();

        assertFalse(result.success());
        assertTrue(result.message().contains("403"));
    }

    @Test
    void createsVaultClientLazily() throws IOException {
        server = startServer(200);
        int[] calls = {0};

        var check = new VaultConnectionCheck(() -> {
            calls[0]++;
            return new VaultClient(url(), new VaultToken("token"));
        }, "health");

        assertEquals(0, calls[0]);
        assertTrue(check.check().success());
        assertEquals(1, calls[0]);
    }

    @Test
    void rejectsBlankPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new VaultConnectionCheck(
                        () -> new VaultClient("http://127.0.0.1:1", new VaultToken("token")), ""));
    }

    private HttpServer startServer(int status) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(status, status == 200 ? 2 : -1);
            if (status == 200) {
                exchange.getResponseBody().write("{}" .getBytes());
            }
            exchange.close();
        });
        server.start();
        return server;
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }
}
