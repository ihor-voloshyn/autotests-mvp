package com.ihorvoloshyn.autotests.rest;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

public class RestClient {

    private final String baseUrl;

    public RestClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        this.baseUrl = baseUrl;
    }

    public Response get(String path) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .get(path);
    }

    public Response get(String path, Map<String, ?> queryParams) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .queryParams(queryParams)
                .get(path);
    }
}