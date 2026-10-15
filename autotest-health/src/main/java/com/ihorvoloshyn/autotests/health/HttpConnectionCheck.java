package com.ihorvoloshyn.autotests.health;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public final class HttpConnectionCheck implements ConnectionCheck {
    private final String name;
    private final String url;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public HttpConnectionCheck(String name, String url) {
        this.name = require(name, "name");
        this.url = require(url, "url");
    }

    @Override public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            return result(status, start);
        } catch (Exception e) {
            return ConnectionCheckResult.failure(name, e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed(start));
        }
    }

    private ConnectionCheckResult result(int status, long start) {
        if (status >= 200 && status < 400)
            return ConnectionCheckResult.success(name, "HTTP endpoint responded with " + status, elapsed(start));
        return ConnectionCheckResult.failure(name, "HTTP endpoint responded with " + status, elapsed(start));
    }
    private static String require(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
