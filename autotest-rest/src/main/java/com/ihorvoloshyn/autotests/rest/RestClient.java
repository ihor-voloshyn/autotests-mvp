package com.ihorvoloshyn.autotests.rest;

import com.ihorvoloshyn.autotests.core.config.Credentials;
import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

public class RestClient {
    private final ConnectionEndpoint endpoint;
    private final Credentials credentials;

    public RestClient(String baseUrl) { this(EndpointResolver.resolve(baseUrl)); }

    public RestClient(ConnectionEndpoint endpoint) {
        this(endpoint, Credentials.empty());
    }

    public RestClient(ConnectionEndpoint endpoint, Credentials credentials) {
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        if (endpoint.scheme() == null) throw new IllegalArgumentException("REST endpoint must include a URL scheme");
        this.endpoint = endpoint;
        this.credentials = credentials == null ? Credentials.empty() : credentials;
    }

    public Response get(String path) { return request(path, Map.of()); }

    public Response get(String path, Map<String, ?> queryParams) { return request(path, queryParams); }

    private Response request(String path, Map<String, ?> queryParams) {
        var request = RestAssured.given()
                .baseUri(endpoint.toUri().toString())
                .queryParams(queryParams);
        if (credentials.isConfigured()) {
            request.auth().preemptive().basic(credentials.username(), credentials.password());
        }
        return request.get(path);
    }
}
