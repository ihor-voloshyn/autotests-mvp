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
    private final VaultToken token;

    public VaultClient(String baseUrl, VaultToken token) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        if (token == null) {
            throw new IllegalArgumentException("token must not be null");
        }
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.token = token;
    }

    public VaultClient(String baseUrl, String token) {
        this(baseUrl, new VaultToken(token));
    }

    public Map<String, Object> read(String path) {
        return get(path);
    }

    public Map<String, Object> readData(String path) {
        Object data = read(path).get("data");
        if (!(data instanceof Map<?, ?>)) {
            throw new IllegalStateException("Vault response does not contain object data");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) data;
        return result;
    }

    public Map<String, Object> readData(String mount, String path, VaultKvVersion version) {
        if (mount == null || mount.isBlank()) {
            throw new IllegalArgumentException("mount must not be blank");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        if (version == null) {
            throw new IllegalArgumentException("version must not be null");
        }

        String endpoint = switch (version) {
            case KV1 -> trimSlashes(mount) + "/" + trimSlashes(path);
            case KV2 -> trimSlashes(mount) + "/data/" + trimSlashes(path);
        };

        Map<String, Object> response = get(endpoint);

        if (version == VaultKvVersion.KV1) {
            return extractMap(response.get("data"), "Vault KV1 response");
        }

        Map<String, Object> outerData = extractMap(response.get("data"), "Vault KV2 response");
        return extractMap(outerData.get("data"), "Vault KV2 response data");
    }

    private Map<String, Object> get(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/" + trimLeadingSlash(path)))
                    .header("X-Vault-Token", token.value())
                    .header("Accept", "application/json")
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
    private static Map<String, Object> extractMap(Object value, String message) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalStateException(message + " does not contain object data");
        }
        return (Map<String, Object>) value;
    }

    private static String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    private static String trimLeadingSlash(String value) {
        return value.replaceAll("^/+", "");
    }

    private static String trimSlashes(String value) {
        return value.replaceAll("^/+|/+$", "");
    }
}
