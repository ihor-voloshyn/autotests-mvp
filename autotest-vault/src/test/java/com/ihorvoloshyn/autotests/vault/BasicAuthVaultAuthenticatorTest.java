package com.ihorvoloshyn.autotests.vault;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicAuthVaultAuthenticatorTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void authenticatesWithBasicCredentials() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        startServer(200, "{\"auth\":{\"client_token\":\"vault-token\"}}", authorization);

        var authenticator = new BasicAuthVaultAuthenticator(
                "http://localhost:" + server.getAddress().getPort(),
                "auth/basic/login",
                "user",
                "password");

        VaultToken token = authenticator.authenticate();

        assertEquals("vault-token", token.value());
        assertEquals(
                "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes(StandardCharsets.UTF_8)),
                authorization.get());
    }

    @Test
    void rejectsAuthenticationWithoutToken() throws Exception {
        startServer(200, "{\"auth\":{}}", new AtomicReference<>());

        var authenticator = new BasicAuthVaultAuthenticator(
                "http://localhost:" + server.getAddress().getPort(),
                "auth/basic/login",
                "user",
                "password");

        assertThrows(IllegalStateException.class, authenticator::authenticate);
    }

    private void startServer(int status, String body, AtomicReference<String> authorization) throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v1/auth/basic/login", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
    }
}
