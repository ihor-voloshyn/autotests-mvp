package com.ihorvoloshyn.autotests.vault;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class VaultClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String token;

    public VaultClient(String baseUrl, String token) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.token = token;
    }

    public Map<String, Object> read(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/" + trimLeadingSlash(path)))
                    .header("X-Vault-Token", token)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Vault request failed: HTTP " + response.statusCode());
            }

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read Vault response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Vault request interrupted", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readData(String path) {
        Object data = read(path).get("data");
        if (!(data instanceof Map<?, ?>)) {
            throw new IllegalStateException("Vault response does not contain object data");
        }
        return (Map<String, Object>) data;
    }

    private static String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    private static String trimLeadingSlash(String value) {
        return value.replaceAll("^/+", "");
    }
}