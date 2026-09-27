package com.ihorvoloshyn.autotests.camunda;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CamundaClient {

    private final String baseUrl;

    public CamundaClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    String baseUrl() {
        return baseUrl;
    }

    public Response get(String path) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .get(path);
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
