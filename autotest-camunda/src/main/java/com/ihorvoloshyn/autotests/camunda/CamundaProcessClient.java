package com.ihorvoloshyn.autotests.camunda;

import io.restassured.response.Response;

import java.util.Map;

public class CamundaProcessClient {

    private final CamundaClient client;

    public CamundaProcessClient(CamundaClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        this.client = client;
    }

    public Response findDefinitions(String processKey) {
        return client.getProcessDefinitions(processKey);
    }

    public Response findInstances(String processKey) {
        return client.getProcessInstances(processKey);
    }

    public Response getInstance(String instanceId) {
        return client.getProcessInstance(instanceId);
    }

    public Response getVariables(String instanceId) {
        return client.get("/engine-rest/process-instance/" + instanceId + "/variables");
    }

    public Response startProcess(String processKey, Map<String, Object> variables) {
        return io.restassured.RestAssured.given()
                .baseUri(getBaseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "variables",
                        variables == null ? Map.of() : variables))
                .post("/engine-rest/process-definition/key/" + processKey + "/start");
    }

    private String getBaseUrl() {
        try {
            var field = CamundaClient.class.getDeclaredField("baseUrl");
            field.setAccessible(true);
            return (String) field.get(client);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access Camunda base URL", e);
        }
    }
}