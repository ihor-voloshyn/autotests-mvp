package com.ihorvoloshyn.autotests.camunda;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;

public class CamundaClient {
    private final ConnectionEndpoint endpoint;

    public CamundaClient(String baseUrl) {
        this(EndpointResolver.resolve(baseUrl));
    }

    public CamundaClient(ConnectionEndpoint endpoint) {
        if (endpoint == null || endpoint.scheme() == null) {
            throw new IllegalArgumentException("Camunda endpoint must include a URL scheme");
        }
        this.endpoint = endpoint.withDefaultPort(8143);
    }

    String baseUrl() {
        return endpoint.toUri().toString().replaceAll("/+$", "");
    }

    public Response get(String path) {
        return request().get(path == null ? "" : path);
    }

    public Response getProcessDefinitions(String key) {
        return request().queryParam("key", key).get("/engine-rest/process-definition");
    }

    public Response getProcessInstances(String processDefinitionKey) {
        return request().queryParam("processDefinitionKey", processDefinitionKey).get("/engine-rest/process-instance");
    }

    private RequestSpecification request() {
        return RestAssured.given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", 5_000)
                                .setParam("http.socket.timeout", 10_000)))
                .baseUri(baseUrl());
    }

    public Response getProcessInstance(String instanceId) {
        return request().pathParam("instanceId", instanceId)
                .get("/engine-rest/process-instance/{instanceId}");
    }
}
