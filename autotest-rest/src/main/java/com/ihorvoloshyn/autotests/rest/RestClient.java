package com.ihorvoloshyn.autotests.rest;

import com.ihorvoloshyn.autotests.core.config.Configuration;
import com.ihorvoloshyn.autotests.core.config.Credentials;
import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class RestClient {
    private final ConnectionEndpoint endpoint;
    private final Credentials credentials;
    private final RestTimeouts timeouts;

    public RestClient(String baseUrl) {
        this(EndpointResolver.resolve(baseUrl));
    }

    public RestClient(ConnectionEndpoint endpoint) {
        this(endpoint, Credentials.empty());
    }

    public RestClient(ConnectionEndpoint endpoint, Credentials credentials) {
        this(endpoint, credentials, RestTimeouts.from(Configuration.get()));
    }

    public RestClient(ConnectionEndpoint endpoint, Credentials credentials, RestTimeouts timeouts) {
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        if (endpoint.scheme() == null) throw new IllegalArgumentException("REST endpoint must include a URL scheme");
        this.endpoint = endpoint.withDefaultPort(defaultPort(endpoint.scheme()));
        this.credentials = credentials == null ? Credentials.empty() : credentials;
        if (timeouts == null) throw new IllegalArgumentException("timeouts must not be null");
        this.timeouts = timeouts;
    }

    public io.restassured.response.Response get(String path) {
        return request(path, Map.of());
    }

    public io.restassured.response.Response get(String path, Map<String, ?> queryParams) {
        return request(path, queryParams);
    }

    private io.restassured.response.Response request(String path, Map<String, ?> queryParams) {
        RequestSpecification request = RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.conn-manager.timeout", (long) timeouts.connectionRequestMillis())
                                .setParam("http.connection.timeout", timeouts.connectMillis())
                                .setParam("http.socket.timeout", timeouts.responseMillis())))
                .baseUri(endpoint.scheme() + "://" + endpoint.authority());

        if (endpoint.path() != null && !endpoint.path().isBlank()) {
            request.basePath(normalizePath(endpoint.path()));
        }
        if (queryParams != null && !queryParams.isEmpty()) request.queryParams(queryParams);
        if (credentials.isConfigured()) {
            request.auth().preemptive().basic(credentials.username(), credentials.password());
        }
        return request.get(path == null ? "" : path);
    }

    private static int defaultPort(String scheme) {
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    private static String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}
