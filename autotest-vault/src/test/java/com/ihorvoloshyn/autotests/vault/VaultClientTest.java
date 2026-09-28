package com.ihorvoloshyn.autotests.vault;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultClientTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void usesDefaultPort8200WhenPortIsNotSpecified() {
        VaultClient client = new VaultClient(
                "http://vault.example.com",
                new VaultToken("token"));

        assertEquals("http://vault.example.com:8200", clientEndpoint(client));
    }

    @Test
    void readsKv1DataUsingToken() throws Exception {
        startServer("/v1/secret/app", 200,
                "{\"data\":{\"username\":\"user\",\"password\":\"pass\"}}");

        VaultClient client = new VaultClient(
                "http://localhost:" + server.getAddress().getPort(),
                new VaultToken("token"));

        var data = client.readData("secret", "app", VaultKvVersion.KV1);

        assertEquals("user", data.get("username"));
        assertEquals("pass", data.get("password"));
    }

    @Test
    void readsKv2DataUsingToken() throws Exception {
        startServer("/v1/secret/data/app", 200,
                "{\"data\":{\"data\":{\"username\":\"user\",\"password\":\"pass\"}}}");

        VaultClient client = new VaultClient(
                "http://localhost:" + server.getAddress().getPort(),
                new VaultToken("token"));

        var data = client.readData("secret", "app", VaultKvVersion.KV2);

        assertEquals("user", data.get("username"));
        assertEquals("pass", data.get("password"));
    }

    @Test
    void rejectsFailedVaultResponse() throws Exception {
        startServer("/v1/secret/app", 403, "{\"errors\":[\"permission denied\"]}");

        VaultClient client = new VaultClient(
                "http://localhost:" + server.getAddress().getPort(),
                new VaultToken("token"));

        assertThrows(IllegalStateException.class,
                () -> client.readData("secret", "app", VaultKvVersion.KV1));
    }

    private void startServer(String path, int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
    }

    private static String clientEndpoint(VaultClient client) {
        try {
            var field = VaultClient.class.getDeclaredField("endpoint");
            field.setAccessible(true);
            var endpoint = (com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint) field.get(client);
            return endpoint.toUri().toString();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot inspect Vault endpoint", e);
        }
    }
}
