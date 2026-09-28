package com.ihorvoloshyn.autotests.reporting;

import io.qameta.allure.Allure;
import java.util.function.Supplier;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public final class AllureSupport {

    private AllureSupport() {
    }

    public static void step(String name, Runnable action) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Allure step name must not be blank");
        }
        if (action == null) {
            throw new IllegalArgumentException("Allure step action must not be null");
        }
        Allure.step(name, action::run);
    }

    public static <T> T step(String name, Supplier<T> action) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Allure step name must not be blank");
        }
        if (action == null) {
            throw new IllegalArgumentException("Allure step action must not be null");
        }
        return Allure.step(name, action::get);
    }

    public static void parameter(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Allure parameter name must not be blank");
        }
        Allure.parameter(name, value == null ? "" : value);
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