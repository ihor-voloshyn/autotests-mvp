package com.ihorvoloshyn.autotests.infrastructure;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ElkClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConnectionEndpoint endpoint;
    private final String authorization;

    public ElkClient(String baseUrl) {
        this(baseUrl, null);
    }

    public ElkClient(String baseUrl, String authorization) {
        this(EndpointResolver.resolve(baseUrl), authorization);
    }

    public ElkClient(ConnectionEndpoint endpoint, String authorization) {
        if (endpoint == null || endpoint.scheme() == null) {
            throw new IllegalArgumentException("ELK endpoint must include a URL scheme");
        }
        this.endpoint = endpoint;
        this.authorization = authorization;
    }

    private String baseUrl() {
        return endpoint.toUri().toString().replaceAll("/+$", "");
    }

    public int healthStatus() {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/_cluster/health"))
                .header("Accept", "application/json")
                .GET();

        applyAuthorization(builder);

        try {
            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot call ELK health endpoint", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ELK health request interrupted", e);
        }
    }

    public String search(String index, String queryJson) {
        if (index == null || index.isBlank()) {
            throw new IllegalArgumentException("index must not be blank");
        }
        if (queryJson == null || queryJson.isBlank()) {
            throw new IllegalArgumentException("queryJson must not be blank");
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/" + index + "/_search"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(queryJson));

        applyAuthorization(builder);

        try {
            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "ELK search failed: HTTP " + response.statusCode() + " " + response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot call ELK", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ELK request interrupted", e);
        }
    }

    private void applyAuthorization(HttpRequest.Builder builder) {
        if (authorization != null && !authorization.isBlank()) {
            builder.header("Authorization", authorization);
        }
    }
}
