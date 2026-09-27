package com.ihorvoloshyn.autotests.health;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class ElkConnectionCheck implements ConnectionCheck {
    private final String url;
    private final HttpClient client = HttpClient.newHttpClient();

    public ElkConnectionCheck(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("url must not be blank");
        this.url = url.replaceAll("/+$", "");
    }

    @Override
    public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url + "/_cluster/health"))
                    .header("Accept", "application/json")
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            return status / 100 == 2
                    ? ConnectionCheckResult.success("ELK", "Elasticsearch health endpoint responded with " + status, elapsed(start))
                    : ConnectionCheckResult.failure("ELK", "Elasticsearch health endpoint responded with " + status, elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure("ELK", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed(start));
        }
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}