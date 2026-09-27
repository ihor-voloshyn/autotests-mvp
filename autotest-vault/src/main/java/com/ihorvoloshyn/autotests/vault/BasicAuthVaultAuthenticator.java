package com.ihorvoloshyn.autotests.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class BasicAuthVaultAuthenticator implements VaultAuthenticator {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String authPath;
    private final String username;
    private final String password;

    public BasicAuthVaultAuthenticator(
            String baseUrl,
            String authPath,
            String username,
            String password) {

        this.baseUrl = require(baseUrl, "baseUrl").replaceAll("/+$", "");
        this.authPath = trimSlashes(require(authPath, "authPath"));
        this.username = require(username, "username");
        this.password = password == null ? "" : password;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public VaultToken authenticate() {
        String credentials = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/" + authPath))
                .header("Authorization", "Basic " + credentials)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Vault authentication failed: HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode token = root.path("auth").path("client_token");

            if (token.isMissingNode() || token.asText().isBlank()) {
                throw new IllegalStateException(
                        "Vault authentication response does not contain auth.client_token");
            }

            return new VaultToken(token.asText());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot authenticate against Vault", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Vault authentication interrupted", e);
        }
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String trimSlashes(String value) {
        return value.replaceAll("^/+|/+$", "");
    }
}
