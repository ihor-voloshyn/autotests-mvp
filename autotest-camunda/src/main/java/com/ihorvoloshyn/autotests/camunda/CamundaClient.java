package com.ihorvoloshyn.autotests.camunda;

import com.ihorvoloshyn.autotests.core.config.Configuration;
import com.ihorvoloshyn.autotests.rest.RestTimeouts;
import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;

public class CamundaClient {
    private final ConnectionEndpoint endpoint;
    private final RestTimeouts timeouts;

    public CamundaClient(String baseUrl) {
        this(EndpointResolver.resolve(baseUrl));
    }

    public CamundaClient(ConnectionEndpoint endpoint) {
        this(endpoint, RestTimeouts.from(Configuration.get()));
    }

    public CamundaClient(ConnectionEndpoint endpoint, RestTimeouts timeouts) {
        if (endpoint == null || endpoint.scheme() == null) {
            throw new IllegalArgumentException("Camunda endpoint must include a URL scheme");
        }
        this.endpoint = endpoint.withDefaultPort(8143);
        if (timeouts == null) throw new IllegalArgumentException("timeouts must not be null");
        this.timeouts = timeouts;
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
                                .setParam("http.conn-manager.timeout", (long) timeouts.connectionRequestMillis())
                                .setParam("http.connection.timeout", timeouts.connectMillis())
                                .setParam("http.socket.timeout", timeouts.responseMillis())))
                .baseUri(baseUrl());
    }

    public Response getProcessInstance(String instanceId) {
        return request().pathParam("instanceId", instanceId)
                .get("/engine-rest/process-instance/{instanceId}");
    }
}
