package com.ihorvoloshyn.autotests.health;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SoapConnectionCheck implements ConnectionCheck {
    private final String url;
    private final String username;
    private final String password;
    private final HttpClient client = HttpClient.newHttpClient();

    public SoapConnectionCheck(String url, String username, String password) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("url must not be blank");
        this.url = url;
        this.username = username;
        this.password = password == null ? "" : password;
    }

    @Override public ConnectionCheckResult check() {
        long start = System.nanoTime();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                    .GET().header("Accept", "text/xml, application/xml");
            if (username != null && !username.isBlank()) {
                String credentials = Base64.getEncoder().encodeToString(
                        (username + ":" + password).getBytes(StandardCharsets.UTF_8));
                builder.header("Authorization", "Basic " + credentials);
            }
            int status = client.send(builder.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            return status >= 200 && status < 400
                    ? ConnectionCheckResult.success("SOAP", "SOAP endpoint responded with " + status, elapsed(start))
                    : ConnectionCheckResult.failure("SOAP", "SOAP endpoint responded with " + status, elapsed(start));
        } catch (Exception e) {
            return ConnectionCheckResult.failure("SOAP", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed(start));
        }
    }
    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
