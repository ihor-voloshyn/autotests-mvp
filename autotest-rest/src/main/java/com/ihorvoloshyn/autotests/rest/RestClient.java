package com.ihorvoloshyn.autotests.rest;

import com.ihorvoloshyn.autotests.core.config.Credentials;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class RestClient {
    private final com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint endpoint;
    private final Credentials credentials;

    public RestClient(String baseUrl) {
        this(EndpointResolver.resolve(baseUrl));
    }

    public RestClient(com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint endpoint) {
        this(endpoint, Credentials.empty());
    }

    public RestClient(
            com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint endpoint,
            Credentials credentials) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint must not be null");
        }
        if (endpoint.scheme() == null) {
            throw new IllegalArgumentException("REST endpoint must include a URL scheme");
        }
        this.endpoint = endpoint;
        this.credentials = credentials == null ? Credentials.empty() : credentials;
    }

    public io.restassured.response.Response get(String path) {
        return request(path, Map.of());
    }

    public io.restassured.response.Response get(String path, Map<String, ?> queryParams) {
        return request(path, queryParams);
    }

    private io.restassured.response.Response request(String path, Map<String, ?> queryParams) {
        RequestSpecification request = RestAssured.given()
                .baseUri(endpoint.scheme() + "://" + endpoint.authority());

        if (endpoint.path() != null && !endpoint.path().isBlank()) {
            request.basePath(normalizePath(endpoint.path()));
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }

        if (credentials.isConfigured()) {
            request.auth().preemptive().basic(
                    credentials.username(),
                    credentials.password());
        }

        return request.get(path == null ? "" : path);
    }

    private static String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}
