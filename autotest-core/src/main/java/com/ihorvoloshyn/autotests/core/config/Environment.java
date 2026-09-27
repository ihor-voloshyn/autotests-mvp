package com.ihorvoloshyn.autotests.core.config;

public enum Environment {
    LOCAL, DEV, TEST, STAGE, PROD;

    public static Environment from(String value) {
        return value == null || value.isBlank()
                ? TEST
                : valueOf(value.trim().toUpperCase());
    }
}
