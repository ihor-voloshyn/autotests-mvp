package com.ihorvoloshyn.autotests.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ElkClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String authorization;

    public ElkClient(String baseUrl) {
        this(baseUrl, null);
    }

    public ElkClient(String baseUrl, String authorization) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.authorization = authorization;
    }

    public String search(String index, String queryJson) {
        if (index == null || index.isBlank()) {
            throw new IllegalArgumentException("index must not be blank");
        }
        if (queryJson == null || queryJson.isBlank()) {
            throw new IllegalArgumentException("queryJson must not be blank");
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + index + "/_search"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(queryJson));

        if (authorization != null && !authorization.isBlank()) {
            builder.header("Authorization", authorization);
        }

        try {
            HttpResponse<String> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

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
}