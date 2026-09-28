package com.ihorvoloshyn.autotests.camunda;

import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import io.restassured.RestAssured;
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
        this.endpoint = endpoint.withDefaultPort(8080);
    }

    String baseUrl() {
        return endpoint.toUri().toString().replaceAll("/+$", "");
    }

    public Response get(String path) {
        return RestAssured.given().baseUri(baseUrl()).get(path);
    }

    public Response getProcessDefinitions(String key) {
        return get("/engine-rest/process-definition?key=" + key);
    }

    public Response getProcessInstances(String processDefinitionKey) {
        return get("/engine-rest/process-instance?processDefinitionKey=" + processDefinitionKey);
    }

    public Response getProcessInstance(String instanceId) {
        return get("/engine-rest/process-instance/" + instanceId);
    }
}
