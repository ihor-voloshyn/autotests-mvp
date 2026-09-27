package com.ihorvoloshyn.autotests.rest;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Map;

public class RestClient {
    private final ConnectionEndpoint endpoint;

    public RestClient(String baseUrl) { this(EndpointResolver.resolve(baseUrl)); }

    public RestClient(ConnectionEndpoint endpoint) {
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        if (endpoint.scheme() == null) throw new IllegalArgumentException("REST endpoint must include a URL scheme");
        this.endpoint = endpoint;
    }

    public Response get(String path) { return request(path, Map.of()); }

    public Response get(String path, Map<String, ?> queryParams) { return request(path, queryParams); }

    private Response request(String path, Map<String, ?> queryParams) {
        String base = endpoint.toUri().toString();
        return RestAssured.given().baseUri(base).queryParams(queryParams).get(path);
    }
}