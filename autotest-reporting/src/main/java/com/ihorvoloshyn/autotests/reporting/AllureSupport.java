package com.ihorvoloshyn.autotests.reporting;

import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public final class AllureSupport {

    private AllureSupport() {
    }

    public static void attachText(String name, String content) {
        Allure.addAttachment(
                name,
                "text/plain",
                new ByteArrayInputStream(
                        (content == null ? "" : content).getBytes(StandardCharsets.UTF_8)),
                ".txt");
    }

    public static void attachJson(String name, String json) {
        Allure.addAttachment(
                name,
                "application/json",
                new ByteArrayInputStream(
                        (json == null ? "" : json).getBytes(StandardCharsets.UTF_8)),
                ".json");
    }
}